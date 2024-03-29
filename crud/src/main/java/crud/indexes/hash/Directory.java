package crud.indexes.hash;

import java.io.IOException;
import java.io.RandomAccessFile;

import logic.SystemSpecification;

/**
 * <strong> The {@code Directory} class represents the directory of a extensible hash index. </strong>
 * 
 * <p>
 * The directory is a file that stores the addresses of the buckets. It is a
 * {@code .db} file that stores the global depth and the addresses of the buckets.
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
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @see crud.indexes.hash.ExtensibleHash
 * @see crud.indexes.hash.Bucket
 * @version 1.0.0
 */
public class Directory implements SystemSpecification {
    
    // Attributes

    private final String path;
    private long[] directory;
    private byte globalDepth = 1;
    private RandomAccessFile file;

    // Constructor

    /**
     * Creates a new Directory with a given path for the {@code .db} file.
     * 
     * @param path Path to the {@code .db} file
     * @throws IOException 
     */
    public Directory(String path) throws IOException {
        if(!path.endsWith(".db"))
            throw new IllegalArgumentException("Path to a extensible hash index must end with \".db\".");

        this.path = INDEXES_FILES_DIRECTORY + path;

        this.file = new RandomAccessFile(this.path, "rw");
        long length = this.file.length();
        this.file.close();

        if(length == 0) {
            this.directory = new long[1 << this.globalDepth];            
            this.toBinaryFile();
        } else {
            this.fromBinaryFile();
        }
    }

    // Public Methods

    /**
     * Returns the path of the Directory file.
     * 
     * @return
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Returns the global depth of the Directory.
     * 
     * @return
     */
    public byte getGlobalDepth() {
        return this.globalDepth;
    }

    /**
     * Returns the addresses stored at the Directory.
     * 
     * @return
     */
    public long[] getDirectory() {
        return this.directory.clone();
    }

    /**
     * Returns the address for the specified key.
     * 
     * @param key Key to be searched
     * @return Address of the key
     */
    public long getAddress(Object key) {
        return this.directory[this.hash(key)];
    }

    /**
     * Sets the address for the specified key in the Directory file.
     * 
     * @param idx Index of the address
     * @param address Address to be set
     * @throws IOException 
     */
    public void setAddress(int idx, long address) throws IOException {
        this.directory[idx] = address;

        this.file = new RandomAccessFile(this.path, "rw");
        this.file.seek(Byte.BYTES + Long.BYTES * idx);
        this.file.writeLong(address);
        this.file.close();
    }

    /**
     * Doubles the size of the Directory.
     * 
     * @throws IOException
     */
    public void doubleSize() throws IOException {
        this.globalDepth++;
        long[] newDirectory = new long[1 << this.globalDepth];

        for(int i = 0; i < this.directory.length; i++) {
            newDirectory[i] = this.directory[i];
            newDirectory[i + this.directory.length] = this.directory[i];
        }
        
        this.directory = newDirectory;
        this.toBinaryFile();
    }

    /**
     * Resets the Directory file.
     * 
     * @throws IOException
     */
    public void reset() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.setLength(0);
        this.file.close();
        
        this.globalDepth = 1;
        this.directory = new long[2];
        this.toBinaryFile();
    }

    public void fromBinaryFile() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.seek(0);

        this.globalDepth = this.file.readByte();
        this.directory = new long[1 << this.globalDepth];
        for(int i = 0; i < this.directory.length; i++) 
            this.directory[i] = this.file.readLong();
        
        this.file.close();
    }

    public void toBinaryFile() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");

        this.file.setLength(0);
        this.file.seek(0);

        this.file.write(this.globalDepth);
        for(int i = 0; i < this.directory.length; i++) 
            this.file.writeLong(this.directory[i]);

        this.file.close();
    }    

    /**
     * Returns the hash of the key for a specified depth.
     * 
     * @param key Key to be hashed
     * @param depth Depth for the hash function
     * @return Hash of the key
     */
    public int reHash(Object key, int depth) {
        return (key.hashCode() < 0 ? - key.hashCode() : key.hashCode()) % (1 << depth);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{ ");

        sb.append("\"globalDepth\": ").append(this.globalDepth).append(", ");
        sb.append("\"directory\": [");
        for(int i = 0; i < this.directory.length; i++) {
            sb.append(this.directory[i]);
            if(i < this.directory.length - 1) sb.append(", ");
        }
            
        return sb.append("] }").toString();
    }

    // Private Methods
    
    /**
     * Hashes the key for the global depth.
     * 
     * @param key Key to be hashed
     * @return Hash of the key
     */
    private int hash(Object key) {
        return (key.hashCode() < 0 ? - key.hashCode() : key.hashCode()) % (1 << this.globalDepth);
    }
}
