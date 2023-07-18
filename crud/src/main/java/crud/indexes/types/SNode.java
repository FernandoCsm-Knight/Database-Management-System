package crud.indexes.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import crud.indexes.types.interfaces.INode;

public class SNode implements INode<SNode> {
    
    // Attributes

    private String key;
    private long value;

    private final int KEY_BYTES = 30;

    // Constructors

    public SNode() {
        this("", -1);
    }

    public SNode(String key, long address) {
        this.key = fit(key);
        this.value = address;
    }

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
        this.key = fit(new String(bf));
        this.value = dis.readLong();

        dis.close();
        bais.close();
    }   

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeBytes(this.key);
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
        sb.append("\"key\": ").append(this.key).append(", ");
        sb.append("\"value\": ").append(this.value).append(" }");
        return sb.toString();
    }

    public static String fit(String str) {
        StringBuilder sb = new StringBuilder(str);
        sb.setLength(30);
        return sb.toString();
    }

}
