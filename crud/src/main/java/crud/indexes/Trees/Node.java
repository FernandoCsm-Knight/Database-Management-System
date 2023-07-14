package crud.indexes.Trees;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Node {
    
    // Attributes

    public static final int BYTES = Integer.BYTES + Long.BYTES;

    public int key;
    public long value;

    public Node() {
        this(-1, -1);
    }

    public Node(int key, long value) {
        this.key = key;
        this.value = value;
    }

    public Node(byte[] buffer) throws IOException {
        this.fromByteArray(buffer);
    }

    // Methods

    public Node clone() {
        return new Node(this.key, this.value);
    }

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


    public void fromByteArray(byte[] buffer) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        DataInputStream dis = new DataInputStream(bais);

        this.key = dis.readInt();
        this.value = dis.readLong();

        dis.close();
        bais.close();
    }

    @Override
    public String toString() {
        return "{ \"key\": " + this.key + ", \"value\": " + this.value + " }";
    }

}
 