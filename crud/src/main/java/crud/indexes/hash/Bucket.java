package crud.indexes.hash;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

import crud.indexes.types.interfaces.INode;

/**
 * <strong> The {@code Bucket} class represents a bucket of a extensible hash index. </strong>
 * 
 * <p>
 * The bucket is a file that stores the keys. It is a {@code .db} file that stores
 * the local depth, the maximum number of keys and the keys.
 * </p>
 * 
 * <p>
 * The bucket is used to store the keys. The keys are stored in the position of
 * the hash of the key. The hash of the key is calculated using the local depth
 * of the bucket.
 * </p>
 * 
 * @author Fernando Campos Silva Dal Maria
 * @see crud.indexes.hash.ExtensibleHash
 * @see crud.indexes.hash.Directory
 * @see crud.indexes.types.interfaces.INode
 * 
 * @version 1.0.0
 */
public class Bucket<T extends INode<T>> {

    // Attributes

    private Constructor<T> constructor; // Constructor of the geneic {@code T extends INode} type.
    private byte localDepth; // Local depth of the bucket
    private int length = 0; // Maximum number of keys
    private int size = 0; // Number of keys
    private T[] keys; // Keys

    public final int BYTES; // Size of the bucket in bytes

    // Constructors

    /**
     * Creates a new bucket with the specified length, local depth and constructor.
     * 
     * @param length Maximum number of keys
     * @param localDepth Local depth of the bucket
     * @param constructor Constructor of the geneic {@code T extends INode} type.
     */
    @SuppressWarnings("unchecked")
    public Bucket(int length, byte localDepth, Constructor<T> constructor) {
        this.length = length;
        this.localDepth = localDepth;
        this.constructor = constructor;
        this.keys = (T[]) new INode[length];

        for (int i = 0; i < this.length; i++) {
            try {
                this.keys[i] = this.constructor.newInstance();
            } catch (Exception e) {
                System.out.println("Can not make a new instance of " + this.constructor.getName() + ".");
                e.printStackTrace();
            }
        }

        this.BYTES = Byte.BYTES + // localDepth
                Integer.BYTES + // length
                length * this.keys[0].getBytes(); // keys
    }

    /**
     * Creates a new bucket from a byte array with the specified length, constructor
     * and local depth.
     * 
     * @param length Maximum number of keys
     * @param buffer Byte array
     * @param constructor Constructor of the geneic {@code T extends INode} type.
     * @throws IOException
     */
    public Bucket(int length, byte[] buffer, Constructor<T> constructor) throws IOException {
        this.length = length;
        this.constructor = constructor;

        this.fromByteArray(buffer);

        this.BYTES = Byte.BYTES + // localDepth
                Integer.BYTES + // length
                length * this.keys[0].getBytes(); // keys
    }

    // Methods

    /**
     * Returns the keys of the bucket.
     * 
     * @return Keys of the bucket
     */
    public T[] getKeys() {
        return this.keys.clone();
    }

    /**
     * Adds a new key to the bucket in the correct position.
     * 
     * @param key Key to be inserted
     * @param value Value of the key
     * @return True if the key was inserted successfully, false otherwise
     * @throws Exception
     */
    public boolean add(Object key, Object value) throws Exception {
        if (this.isFull())
            throw new IndexOutOfBoundsException("Cannot insert elemenet because bucket is full.");

        T node = this.constructor.newInstance();
        node.setKey(key);
        node.setValue(value);

        int i = 0;
        for (i = this.size - 1; i >= 0 && this.keys[i].compareTo(key) > 0; i--)
            this.keys[i + 1] = this.keys[i];

        this.keys[i + 1] = node;
        this.size++;
        return true;
    }

    /**
     * Updates the value of a key in the bucket.
     * Only updates the first key found.
     * 
     * @param key Key to be updated
     * @param value New value of the key
     * @return True if the key was updated successfully, false otherwise
     * @throws Exception
     */
    public boolean update(Object key, Object value) throws Exception {
        if (this.isEmpty())
            return false;

        T node = this.constructor.newInstance();
        node.setKey(key);
        node.setValue(value);

        boolean found = false;
        for (int i = 0; i < this.size && !found; i++) {
            found = this.keys[i].compareTo(key) == 0;
            if (found)
                this.keys[i] = node;
        }

        return found;
    }

    /**
     * Updates the value of a key in the bucket.
     * Updates the first key with the specified {@code oldValue}.
     * 
     * @param key Key to be updated
     * @param oldValue Old value of the key
     * @param newValue New value of the key
     * @return True if the key was updated successfully, false otherwise
     * @throws Exception
     */
    public boolean update(Object key, Object oldValue, Object newValue) throws Exception {
        if (this.isEmpty())
            return false;

        T node = this.constructor.newInstance();
        node.setKey(key);
        node.setValue(oldValue);

        boolean found = false;
        for (int i = 0; i < this.size && !found; i++) {
            found = this.keys[i].equals(node);
            if (found) {
                node.setValue(newValue);
                this.keys[i] = node;
            }
        }

        return found;
    }

    /**
     * Deletes a key from the bucket.
     * Only deletes the first key found.
     * 
     * @param key Key to be deleted
     * @return True if the key was deleted successfully, false otherwise
     * @throws Exception
     */
    public boolean delete(Object key) throws Exception {
        if (this.isEmpty())
            return false;

        boolean found = false;
        for (int i = 0; i < this.size && !found; i++) {
            found = this.keys[i].compareTo(key) == 0;
            if (found) {
                for (int j = i; j < this.size - 1; j++)
                    this.keys[j] = this.keys[j + 1];

                this.keys[this.size - 1] = this.constructor.newInstance();
                this.size--;
            }
        }

        return found;
    }

    /**
     * Deletes a key from the bucket.
     * Deletes the first key with the specified {@code value}.
     * 
     * @param key Key to be deleted
     * @param value Value of the key
     * @return True if the key was deleted successfully, false otherwise
     * @throws Exception
     */
    public boolean delete(Object key, Object value) throws Exception {
        if (this.isEmpty())
            return false;

        T node = this.constructor.newInstance();
        node.setKey(key);
        node.setValue(value);

        boolean found = false;
        for (int i = 0; i < this.size && !found; i++) {
            found = this.keys[i].equals(node);
            if (found) {
                for (int j = i; j < this.size - 1; j++)
                    this.keys[j] = this.keys[j + 1];

                this.keys[this.size - 1] = this.constructor.newInstance();
                this.size--;
            }
        }

        return found;
    }

    /**
     * Searches for a key in the bucket.
     * 
     * @param key Key to be searched
     * @return The key if it was found, null otherwise
     */
    public T search(Object key) {
        for (int i = 0; i < this.size; i++)
            if (this.keys[i].compareTo(key) == 0)
                return this.keys[i];

        return null;
    }

    /**
     * Searches for a key in the bucket.
     * 
     * @param key Key to be searched
     * @return True if it was found, false otherwise
     */
    public boolean contains(Object key) {
        boolean exists = false;
        for (int i = 0; i < keys.length && !exists; i++)
            exists = this.keys[i].compareTo(key) == 0;

        return exists;
    }

    /**
     * Searches for a key with the given {@code value} in the bucket.
     * 
     * @param key Key to be searched
     * @param value Value of the key 
     * @return True if it was found, false otherwise
     * @throws Exception
     */
    public boolean contains(Object key, Object value) throws Exception {
        T node = this.constructor.newInstance();
        node.setKey(key);
        node.setValue(value);

        boolean exists = false;
        for (int i = 0; i < keys.length && !exists; i++)
            exists = this.keys[i].equals(node);

        return exists;
    }

    /**
     * Returns the local depth of the bucket.
     * 
     * @return Local depth of the bucket
     */ 
    public int getLocalDepth() {
        return this.localDepth;
    }

    /**
     * Returns how many keys the bucket has.
     * 
     * @return Number of keys in the bucket
     */
    public int size() {
        return this.size;
    }

    /**
     * Returns if the bucket is full.
     * 
     * @return True if the bucket is full, false otherwise
     */
    public boolean isFull() {
        return this.size == this.length;
    }

    /**
     * Returns if the bucket is empty.
     * 
     * @return True if the bucket is empty, false otherwise
     */
    public boolean isEmpty() {
        return this.size == 0;
    }

    // Read and Write

    /**
     * Reads the bucket from a byte array.
     * 
     * @param buffer Byte array that represents the bucket
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void fromByteArray(byte[] buffer) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        DataInputStream dis = new DataInputStream(bais);

        this.localDepth = dis.readByte();
        this.size = dis.readInt();
        this.keys = (T[]) new INode[this.length];

        for (int i = 0; i < this.length; i++) {
            try {
                this.keys[i] = this.constructor.newInstance();
            } catch (Exception e) {
                System.out.println("Can not make a new instance of " + this.constructor.getName() + ".");
                e.printStackTrace();
            }

            byte[] b = new byte[this.keys[i].getBytes()];
            dis.read(b);

            this.keys[i].fromByteArray(b);
        }

        dis.close();
        bais.close();
    }

    /**
     * Converts the bucket to a byte array.
     * 
     * @return the byte array that represents the bucket
     * @throws IOException
     */
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeByte(this.localDepth);
        dos.writeInt(this.size);
        for (int i = 0; i < this.length; i++)
            dos.write(this.keys[i].toByteArray());

        byte[] buffer = baos.toByteArray();
        dos.close();
        baos.close();

        return buffer;
    }

    // Override

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("{\n ");
        sb.append("\t\"localDepth\": ").append(this.localDepth).append(", ");
        sb.append("\"size\": ").append(this.size).append(",\n\t ");
        sb.append("\"keys\": [");
        for (int i = 0; i < this.size; i++) {
            sb.append(this.keys[i].toString());
            if (i < this.size - 1)
                sb.append(", ");
        }
        return sb.append("]\n}").toString();
    }
}
