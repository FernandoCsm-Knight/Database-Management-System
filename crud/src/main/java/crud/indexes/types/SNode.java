package crud.indexes.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import crud.indexes.types.interfaces.INode;

/**
 * <strong> A {@code INode} implementation for String-long indexes. </strong>
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class SNode implements INode<SNode> {
    
    // Attributes

    private final static int KEY_BYTES = 50; // Size of the key in bytes

    private String key; // Key
    private long value; // Value

    // Constructors

    /**
     * Default constructor initializes the node with an empty string key and a value of -1.
     */
    public SNode() {
        this("", -1);
    }

    /**
     * Parameterized constructor initializes the node with the specified key and value.
     * 
     * @param key The key (String) for the node.
     * @param address The value (long) for the node.
     */
    public SNode(String key, long address) {
        this.key = fit(key);
        this.value = address;
    }

    /**
     * Constructor that creates an SNode object from a byte array.
     * 
     * @param buffer The byte array containing the serialized node data.
     * @throws IOException If there is an issue with deserialization.
     */
    public SNode(byte[] buffer) throws IOException {
        this.fromByteArray(buffer);
    }

    // Methods

    @Override
    public Object getKey() {
        return this.key;
    }

    @Override
    public Object getValue() {
        return this.value;
    }
    
    @Override
    public void setKey(Object key) {
        if(!(key instanceof String))
            throw new IllegalArgumentException("The key must be a String.");
        this.key = fit((String)key);
    }

    @Override 
    public void setValue(Object address) {
        if(!(address instanceof Long))
            throw new IllegalArgumentException("The address must be a Long.");
        this.value = (Long)address;
    }

    @Override
    public int getBytes() {
        return KEY_BYTES + Long.BYTES;
    }

    // Read and Write

    @Override
    public void fromByteArray(byte[] buffer) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        DataInputStream dis = new DataInputStream(bais);

        byte[] bf = new byte[KEY_BYTES];
        dis.read(bf);
        this.key = fit(new String(bf, StandardCharsets.UTF_8));
        this.value = dis.readLong();

        dis.close();
        bais.close();
    }   

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.write(this.key.getBytes(StandardCharsets.UTF_8));
        dos.writeLong(this.value);

        byte[] buffer = baos.toByteArray();
        dos.close();
        baos.close();

        return buffer;
    }

    @Override
    public boolean equals(Object other) {
        return this.key.equals(((SNode)other).key) && this.value == ((SNode)other).value;
    }

    @Override 
    public int compareTo(Object key) {
        return (key instanceof String) ? this.key.compareTo((String)key) : this.key.compareTo(((SNode)key).key);
    }

    @Override
    public SNode clone() {
        return new SNode(this.key, this.value);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("{ ");
        sb.append("\"key\": ").append("\"").append(unfit(this.key).replace("\"", "\'")).append("\"").append(", ");
        sb.append("\"value\": ").append(this.value).append(" }");
        return sb.toString();
    }

    /**
     * Fits a string to the specified key length, padding with spaces if necessary.
     * 
     * @param str The input string.
     * @return The fitted string.
     */
    public static String fit(String str) {
        if(str.length() > KEY_BYTES)
            str = str.substring(0, KEY_BYTES);
        else if(str.length() < KEY_BYTES)
            str += " ".repeat((KEY_BYTES - str.getBytes(StandardCharsets.UTF_8).length)/" ".getBytes(StandardCharsets.UTF_8).length);

        return str;
    }

    /**
     * Removes trailing spaces from a string.
     * 
     * @param str The input string.
     * @return The string with trailing spaces removed.
     */
    public static String unfit(String str) {
        return str.trim();
    }

}
