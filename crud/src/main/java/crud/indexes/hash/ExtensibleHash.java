package crud.indexes.hash;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.UUID;

import crud.base.StructureValidation;
import crud.indexes.types.interfaces.INode;
import logic.Logic;
import logic.SystemSpecification;

/**
 * <strong> A generic Extensible Hash implementation. </strong>
 * 
 * <p>
 * The Extensible Hash is a dynamic hash index. It is used to index the
 * registers in the database. It is a {@code .db} file that stores the
 * directory and the buckets.
 * </p>
 * 
 * <p>
 * The directory is a file that stores the addresses of the buckets. It is a
 * {@code .db} file that stores the global depth and the addresses of the
 * buckets.
 * </p>
 * 
 * <p>
 * The directory is used to find the address of a key. The address of the key is
 * stored in the bucket that is in the position of the hash of the key. The hash
 * of the key is calculated using the global depth of the directory.
 * </p>
 * 
 * <p>
 * The directory is also used to double the size of the index. When the number of
 * keys in a bucket is greater than the maximum number of keys, the size of the
 * directory is doubled. The global depth is incremented and the addresses of the
 * buckets are duplicated.
 * </p>
 * 
 * <p>
 * The buckets are the blocks of the index. They store the keys and the
 * addresses of the registers that contain them. The maximum number of keys in a
 * bucket is specified in the constructor.
 * </p>
 * 
 * @author Fernando Campos Silva Dal Maria
 * @see crud.indexes.hash.Directory
 * @see crud.indexes.hash.Bucket
 * 
 * @version 1.0.0
 */
public class ExtensibleHash<T extends INode<T>> implements SystemSpecification {
    
    // Attributes

    private String path; // Path of the index
    private Directory directory; // Directory of the index
    private RandomAccessFile file; // File of the index
    private Constructor<T> constructor; // Constructor of the keys
    private boolean isRedundant = false; // If the index is redundant
    
    private final int bucketLength; // Maximum number of keys in a bucket
    public final int BUCKET_BYTES; // Size of a bucket in bytes

    // Constructor

    /**
     * Creates a new ExtensibleHash with a given path for the {@code .db} file.
     * 
     * @param path Path to the {@code .db} file
     * @param constructor Constructor of the keys
     * @throws Exception
     */
    public ExtensibleHash(String path, Constructor<T> constructor) throws Exception {
        if(!path.endsWith(".db"))
            throw new IllegalArgumentException("Path to a extensible hash index must end with \".db\".");

        StructureValidation.createIndexesDirectory();

        File file = new File(INDEXES_FILES_DIRECTORY);
        String[] filePaths = file.list();

        String directoryPath = UUID.randomUUID().toString() + path;
        for(int i = 0; i < filePaths.length; i++) 
            if(filePaths[i].endsWith(path) && !filePaths[i].equals(path))
                directoryPath = filePaths[i];

        this.constructor = constructor;
        this.path = INDEXES_FILES_DIRECTORY + path;
        this.directory = new Directory(directoryPath);
        this.bucketLength = Logic.database.blockFactor(1000);
        this.BUCKET_BYTES = new Bucket<T>(bucketLength, (byte)0, constructor).BYTES;
        this.init();
    }

    public ExtensibleHash(String path, Constructor<T> constructor, boolean isRedundant) throws Exception {
        this(path, constructor);
        this.isRedundant = isRedundant;
    }

    // Public Methods

    /**
     * Returns the path of the index file.
     * 
     * @return Path of the index file
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Resets the index rewriting the original file.
     * 
     * @throws IOException
     */
    public void reset() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.setLength(0);
        this.file.close();

        this.directory.reset();
        this.init();
    }

    /**
     * Inserts a new key in the index using the specified 
     * {@code INode} structure.
     * 
     * @param key Key to be inserted
     * @param value Value of the key
     * @return True if the key was inserted successfully, false otherwise
     * @throws Exception
     */
    public boolean insert(Object key, Object value) throws Exception {
        long address = this.directory.getAddress(key);
        Bucket<T> bucket = this.readBucket(address);

        if((!this.isRedundant && bucket.contains(key)) || (this.isRedundant && bucket.contains(key, value))) 
            return false;

        if(bucket.isFull()) {
            T[] keys = bucket.getKeys();
            byte localDepth = (byte)(bucket.getLocalDepth() + 1);

            if(bucket.getLocalDepth() == this.directory.getGlobalDepth()) 
                this.directory.doubleSize();

            bucket = new Bucket<>(bucketLength, localDepth, constructor);
            this.writeBucket(bucket, address);

            long newAddress = this.length();
            this.writeBucket(bucket, newAddress);

            boolean replace = false;
            int start = this.directory.reHash(key, localDepth - 1);
            int end = 1 << this.directory.getGlobalDepth();
            for(int i = start; i < end; i += 1 << (localDepth - 1)) {
                if(replace) this.directory.setAddress(i, newAddress);
                replace = !replace;
            }

            for(int i = 0; i < keys.length; i++) {
                this.insert(keys[i].getKey(), keys[i].getValue());
            }
            this.insert(key, value);
        } else {
            bucket.add(key, value);
            this.writeBucket(bucket, address);
        }

        return true;
    }

    /**
     * Deletes a key from a non-redundant index.
     * 
     * @param key Key to be deleted
     * @return True if the key was deleted successfully, false otherwise
     * @throws Exception
     */
    public boolean delete(Object key) throws Exception {
        if(this.isRedundant) 
            throw new IllegalAccessError("This index is redundant, if you want to delete a key use delete(Object key, Object value).");
        
        long address = this.directory.getAddress(key);
        Bucket<T> bucket = this.readBucket(address);
        boolean res = bucket.delete(key);
        this.writeBucket(bucket, address);
        return res;

    }

    /**
     * Deletes a key from a redundant index.
     * 
     * @param key Key to be deleted
     * @param value Value of the key
     * @return True if the key was deleted successfully, false otherwise
     * @throws Exception 
     */
    public boolean delete(Object key, Object value) throws Exception {
        long address = this.directory.getAddress(key);
        Bucket<T> bucket = this.readBucket(address);
        boolean res = bucket.delete(key, value);
        this.writeBucket(bucket, address);
        return res;
    }

    /**
     * Updates a key from a non-redundant index.
     * 
     * @param key Key to be updated
     * @param value New value of the key
     * @return True if the key was updated successfully, false otherwise
     * @throws Exception
     */
    public boolean update(Object key, Object value) throws Exception {
        if(this.isRedundant) 
            throw new IllegalAccessError("This index is redundant, if you want to update a key use update(Object key, Object oldValue, Object newValue).");
        
        long address = this.directory.getAddress(key);
        Bucket<T> bucket = this.readBucket(address);
        boolean res = bucket.update(key, value);
        this.writeBucket(bucket, address);
        return res;
    }

    /**
     * Updates a key from a redundant index.
     * 
     * @param key Key to be updated
     * @param oldValue Old value of the key
     * @param newValue New value of the key
     * @return True if the key was updated successfully, false otherwise
     * @throws Exception
     */
    public boolean update(Object key, Object oldValue, Object newValue) throws Exception {
        long address = this.directory.getAddress(key);
        Bucket<T> bucket = this.readBucket(address);
        boolean res = bucket.update(key, oldValue, newValue);
        this.writeBucket(bucket, address);
        return res;
    }

    /**
     * Searches a key in the index.
     * 
     * @param key Key to be searched
     * @return The key if it was found, null otherwise
     * @throws IOException 
     */
    public T search(Object key) throws IOException {
        long address = this.directory.getAddress(key);
        Bucket<T> bucket = this.readBucket(address);
        return bucket.search(key);
    }

    /**
     * Returns all the keys in the index that match with the specified key.
     * 
     * @param key Key to be searched
     * @return The keys if they were found, null otherwise
     * @throws IOException 
     */
    public T[] readAll(Object key) throws IOException {
        long address = this.directory.getAddress(key);
        Bucket<T> bucket = this.readBucket(address);
        return bucket.getKeys();
    }

    /**
     * Prints the index in a {@code .json} file.
     * 
     * @throws IOException
     */
    public void toJsonFile() throws IOException {
        StructureValidation.createJSONIndexDirectory();
        String[] strs = this.path.split("/");

        HashSet<Long> set = new HashSet<>();
        long[] addresses = this.directory.getDirectory();
        StringBuffer sb = new StringBuffer("{\n\"buckets\": [\n");
        for(int i = 0; i < addresses.length; i++) {
            if(!set.contains(addresses[i])) {
                set.add(addresses[i]);
                sb.append(this.readBucket(addresses[i]) + ",\n");
            }
        }

        sb.delete(sb.length() - 2, sb.length());
        sb.append("\n]\n}");

        BufferedWriter bw = new BufferedWriter(new FileWriter(JSON_INDEXES_DIRECTORY + strs[strs.length - 1].replace(".db", ".json")));
        bw.write("[\n");
        bw.write(this.directory.toString() + ",\n");
        bw.write(sb.toString());
        bw.write("\n]");
        bw.close();
    }

    // Private Methods

    /**
     * Initializes the index.
     * 
     * @throws IOException
     */
    private void init() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        if(this.file.length() == 0) 
            this.file.write(new Bucket<T>(bucketLength, (byte)0, constructor).toByteArray());
        
        this.file.close();
    }

    /**
     * Returns the length of the index file.
     * 
     * @return Length of the index file
     * @throws IOException
     */
    private long length() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        long length = this.file.length();
        this.file.close();
        return length;
    }

    /**
     * Reads a bucket from the index file at the given address.
     * 
     * @param address Address of the bucket
     * @return The bucket if it was found, null otherwise
     * @throws IOException
     */
    private Bucket<T> readBucket(long address) throws IOException {
        if(address == -1) return null;

        this.file = new RandomAccessFile(this.path, "rw");
        this.file.seek(address);
        byte[] buffer = new byte[this.BUCKET_BYTES];
        this.file.read(buffer);
        this.file.close();

        return new Bucket<T>(bucketLength, buffer, this.constructor);
    }

    /**
     * Writes a bucket in the index file at the given address.
     * 
     * @param bucket Bucket to be written
     * @param address Address of the bucket
     * @throws IOException
     */
    private void writeBucket(Bucket<T> bucket, long address) throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.seek(address);
        this.file.write(bucket.toByteArray());
        this.file.close();
    }

    // If you want to filter some words from the index, use this method
    //
    // private String[] filter(String[] strs) {
    //     HashSet<String> hash = new HashSet<>();

    //     hash.add("a");
    //     hash.add("o");
    //     hash.add("ao");
    //     hash.add("da");
    //     hash.add("do");
    //     hash.add("das");
    //     hash.add("dos");
    //     hash.add("de");
    //     hash.add("e");

    //     ArrayList<String> arr = new ArrayList<>();

    //     for(int i = 0; i < strs.length; i++) 
    //         if(!hash.contains(strs[i])) 
    //             arr.add(strs[i]);

    //     String[] newStrs = new String[arr.size()];
    //     return arr.toArray(newStrs);
    // }
}
