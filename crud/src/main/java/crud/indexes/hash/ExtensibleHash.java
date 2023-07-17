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



public class ExtensibleHash<T extends INode<T>> implements SystemSpecification {
    
    // Attributes

    private String path;
    private Directory directory;
    private RandomAccessFile file;
    private Constructor<T> constructor;
    
    private final int bucketLength;
    public final int BUCKET_BYTES;

    // Constructor

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
        this.bucketLength = Logic.database.blockFactor(10);
        this.BUCKET_BYTES = new Bucket<T>(bucketLength, (byte)0, constructor).BYTES;
        this.init();
    }

    // Public Methods

    public void reset() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.setLength(0);
        this.file.close();

        this.directory.reset();
        this.init();
    }

    public void insert(Object key, Object value) throws Exception {
        long address = this.directory.getAddress(key);
        Bucket<T> bucket = this.readBucket(address);

        if(bucket.contains(key))
            throw new IllegalArgumentException("Key " + key.toString() + " already exists.");

        if(bucket.isFull()) {
            T[] keys = bucket.keys;
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
    }

    public boolean delete(Object key) throws Exception {
        long address = this.directory.getAddress(key);
        Bucket<T> bucket = this.readBucket(address);
        boolean res = bucket.delete(key);
        this.writeBucket(bucket, address);
        return res;

    }

    public boolean update(Object key, Object value) throws Exception {
        long address = this.directory.getAddress(key);
        Bucket<T> bucket = this.readBucket(address);
        boolean res = bucket.update(key, value);
        this.writeBucket(bucket, address);
        return res;
    }

    public T search(Object key) throws Exception {
        long address = this.directory.getAddress(key);
        Bucket<T> bucket = this.readBucket(address);
        return bucket.search(key);
    }

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

    private void init() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        if(this.file.length() == 0) 
            this.file.write(new Bucket<T>(bucketLength, (byte)0, constructor).toByteArray());
        
        this.file.close();
    }

    private long length() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        long length = this.file.length();
        this.file.close();
        return length;
    }

    private Bucket<T> readBucket(long address) throws IOException {
        if(address == -1) return null;

        this.file = new RandomAccessFile(this.path, "rw");
        this.file.seek(address);
        byte[] buffer = new byte[this.BUCKET_BYTES];
        this.file.read(buffer);
        this.file.close();

        return new Bucket<T>(bucketLength, buffer, this.constructor);
    }

    private void writeBucket(Bucket<T> bucket, long address) throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.seek(address);
        this.file.write(bucket.toByteArray());
        this.file.close();
    }
}
