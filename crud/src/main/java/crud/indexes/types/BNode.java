package crud.indexes.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import crud.indexes.types.interfaces.INode;

public class BNode implements INode<BNode> {
    
    // Attributes

    public static final int BYTES = Integer.BYTES + Long.BYTES;

    private int key;
    private long value;

    public BNode() {
        this(-1, -1);
    }

    public BNode(int key, long value) {
        this.key = key;
        this.value = value;
    }

    public BNode(byte[] buffer) throws IOException {
        this.fromByteArray(buffer);
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

        dos.writeInt(key);
        dos.writeLong(value);

        byte[] buffer = baos.toByteArray();
        
        dos.close();
        baos.close();
        return buffer;
    }

    @Override
    public int compareTo(Object other) {
        return other instanceof Integer ? this.key - key : this.key - ((BNode)other).key;
    }

    @Override
    public BNode clone() {
        return new BNode(this.key, this.value);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("{ ");
        sb.append("\"key\": ").append(this.key).append(", ");
        sb.append("\"value\": ").append(this.value).append(" }");
        return sb.toString();
    }

}
 