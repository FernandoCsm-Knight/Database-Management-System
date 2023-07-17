package crud.indexes.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import crud.indexes.types.interfaces.INode;

public class HNode implements INode<HNode> {
 
    // Attributes

    public static final int BYTES = Integer.BYTES + Long.BYTES;

    private int key = -1;
    private long value = -1;

    // Constructor

    public HNode() {
        this(-1, -1);
    }

    public HNode(int key, long value) {
        this.key = key;
        this.value = value;
    }

    // Methods

    @Override
    public void setKey(Object key) {
        if(key == null) 
            throw new NullPointerException("Key can not be null.");
        this.key = (Integer)key;
    }

    @Override
    public void setValue(Object value) {
        if(value == null) 
            throw new NullPointerException("Value can not be null.");
        this.value = (Long)value;
    }

    @Override
    public Object getKey() {
        return this.key;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public int getBytes() {
        return BYTES;
    }

    @Override
    public void fromByteArray(byte[] buffer) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        DataInputStream dis = new DataInputStream(bais);

        this.key = dis.readInt();
        this.value = dis.readLong();
        
        dis.close();
        bais.close();
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        byte[] buffer = new byte[BYTES];

        dos.writeInt(this.key);
        dos.writeLong(this.value);

        buffer = baos.toByteArray();

        dos.close();
        baos.close();

        return buffer;
    }

    @Override
    public int compareTo(Object other) {
        if(other == null) 
            throw new NullPointerException("Other can not be null.");
        return (other instanceof Integer) ? this.key - (Integer)other : this.key - ((HNode)other).key;
    }

    @Override
    public HNode clone() {
        return new HNode(this.key, this.value);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("{ ");
        sb.append("\"key\": ").append(this.key).append(", ");
        sb.append("\"value\": ").append(this.value).append(" }");
        return sb.toString();
    }

}
