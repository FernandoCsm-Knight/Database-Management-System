package crud.indexes.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import crud.indexes.types.interfaces.INode;

/**
 * <strong> A {@code INode} implementation for int-long indexes. </strong>
 * 
 * @author Fernando Campos Silva Dal Maria
 * @version 1.0.0
 */
public class NNode implements INode<NNode> {
    
    // Attributes

    public static final int BYTES = Integer.BYTES + Long.BYTES; // Size of the node in bytes

    private int key; 
    private long value;

    public NNode() {
        this(-1, -1);
    }

    public NNode(int key, long value) {
        this.key = key;
        this.value = value;
    }

    public NNode(byte[] buffer) throws IOException {
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
    public boolean equals(Object other) {
        return this.key == ((NNode)other).key && this.value == ((NNode)other).value;
    }

    @Override
    public int compareTo(Object other) {
        return other instanceof Integer ? this.key - key : this.key - ((NNode)other).key;
    }

    @Override
    public NNode clone() {
        return new NNode(this.key, this.value);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("{ ");
        sb.append("\"key\": ").append(this.key).append(", ");
        sb.append("\"value\": ").append(this.value).append(" }");
        return sb.toString();
    }

}
 