package crud.indexes.trees;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import crud.indexes.types.interfaces.INode;

/**
 * <strong> A generic Page implementation for a B+ tree. </strong>
 * 
 * <p>
 * A page is a node in the tree. It can be a leaf or an internal node.
 * The page is a block of bytes in the disk, so it has a fixed size.
 * The order specifies the maximum number of keys that a page can have.
 * </p>
 * 
 * <p>
 * The page is used to find the address of a key. The address of the key is
 * stored in the leaf that contains the key. The leaf is found by searching
 * the tree.
 * </p>
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @see crud.indexes.trees.BPlusTree
 * @see crud.indexes.types.interfaces.INode
 * @version 1.0.0
 */
public class Page<T extends INode<T>> {
    
    // Attributes

    private int order; // Maximum number of keys
    private final Constructor<T> constructor; // Constructor of the keys

    public T[] keys; // Keys
    public long[] children; // Children

    public int keyCount = 0; // Number of keys
    public long address = -1; // Address of the page in the disk
    public long next = -1; // Address of the next page in the disk

    public final int BYTES; // Size of the page in bytes

    // Constructors

    /**
     * Creates a new page with the specified order and constructor.
     * 
     * @param order Maximum number of keys
     * @param constructor Constructor of the keys
     */
    @SuppressWarnings("unchecked")
    public Page(int order, Constructor<T> constructor) {
        this.order = order;
        this.constructor = constructor;
        this.keys = (T[])new INode[order];
        this.children = new long[order + 1];
        this.next = -1;

        for(int i = 0; i < this.order; i++) {
            try {
                this.keys[i] = constructor.newInstance();
            } catch(Exception e) {
                System.out.println("Can not make a new instance of " + constructor.getName() + ".");
                e.printStackTrace();
            }
            this.children[i] = -1;
        }

        this.children[this.order] = -1;

        this.BYTES = Integer.BYTES + // order
                     Integer.BYTES + // keyCount
                     order * this.keys[0].getBytes() + // keys
                     (order + 1) * Long.BYTES + // children
                     Long.BYTES; // next
    }

    /**
     * Creates a new page from a byte array with the specified constructor.
     * 
     * @param buffer Byte array
     * @param constructor Constructor of the keys
     * @throws IOException
     */
    public Page(byte[] buffer, Constructor<T> constructor) throws IOException {
        this.constructor = constructor;
        this.fromByteArray(buffer);
        this.BYTES = Integer.BYTES + // order
                     Integer.BYTES + // keyCount
                     order * this.keys[0].getBytes() + // keys
                     (order + 1) * Long.BYTES + // children
                     Long.BYTES; // next
    }


    // Methods

    /**
     * Identifies if the page has underflow.
     * 
     * @return True if the page has underflow, false otherwise
     */
    public boolean isUnderflow() {
        return this.keyCount < (int)(Math.ceil(this.order / 2.0)) - 1;
    }

    /**
     * Identifies if the page can borrow a key.
     * 
     * @return True if the page can borrow a key, false otherwise
     */
    public boolean canBorrow() {
        return this.keyCount > (int)(Math.ceil(this.order / 2.0)) - 1;
    }

    /**
     * Converts the page to a byte array that can be written.
     * 
     * @return Byte array with the byte version of the page
     * @throws IOException
     */
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeInt(this.order);
        dos.writeInt(this.keyCount);

        for(int i = 0; i < this.order; i++) {
            dos.writeLong(this.children[i]);
            dos.write(this.keys[i].toByteArray());
        }

        dos.writeLong(this.children[this.order]);
        dos.writeLong(this.next);

        byte[] buffer = baos.toByteArray();

        dos.close();
        baos.close();
        return buffer;
    }

    /**
     * Converts a byte array to a page.
     * 
     * @param buffer Byte array with the byte version of the page
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void fromByteArray(byte[] buffer) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        DataInputStream dis = new DataInputStream(bais);

        this.order = dis.readInt();
        this.keyCount = dis.readInt();

        this.keys = (T[])new INode[order];
        this.children = new long[this.order + 1];

        for(int i = 0; i < this.order; i++) {
            this.children[i] = dis.readLong();

            try {
                this.keys[i] = constructor.newInstance();
            } catch(Exception e) {
                System.out.println("Can not make a new instance of " + constructor.getName() + ".");
                e.printStackTrace();
            }

            byte[] nodeBuffer = new byte[this.keys[i].getBytes()];
            dis.read(nodeBuffer);

            this.keys[i].fromByteArray(nodeBuffer);
        }

        this.children[this.order] = dis.readLong();
        this.next = dis.readLong();

        dis.close();
        bais.close();
    }

    /**
     * Converts the page to a JSON representation.
     * 
     * @return a String representation for the page
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("{\n\t");
        sb.append("\"address\": ").append(this.address);
        sb.append(",\n\t\"order\": ").append(this.order);
        sb.append(",\n\t\"keyCount\": ").append(this.keyCount);
        sb.append(",\n\t\"keys\": ").append(Arrays.toString(this.keys));
        sb.append(",\n\t\"children\": ").append(Arrays.toString(this.children));
        sb.append(",\n\t\"next\": ").append(this.next);
        return  sb.append("\n}").toString();
    }

}
