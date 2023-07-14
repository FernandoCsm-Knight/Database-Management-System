package crud.indexes.Trees;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Page {
    
    // Attributes

    private int order;

    public Node[] keys;
    public long[] children;

    public int keyCount = 0;
    public long address = -1;
    public long next = -1;

    public final int BYTES;

    // Constructors
    public Page(int order) {
        this.order = order;
        this.keys = new Node[order];
        this.children = new long[order + 1];
        this.next = -1;

        for(int i = 0; i < this.order; i++) {
            this.keys[i] = new Node();
            this.children[i] = -1;
        }

        this.children[this.order] = -1;

        this.BYTES = Integer.BYTES + // order
                     Integer.BYTES + // keyCount
                     order * Node.BYTES + // keys
                     (order + 1) * Long.BYTES + // children
                     Long.BYTES; // next
    }

    public Page(byte[] buffer) throws IOException {
        this.fromByteArray(buffer);
        this.BYTES = Integer.BYTES + // order
                     Integer.BYTES + // keyCount
                     order * Node.BYTES + // keys
                     (order + 1) * Long.BYTES + // children
                     Long.BYTES; // next
    }


    // Methods

    public boolean isUnderflow() {
        return this.keyCount < (int)(Math.ceil(this.order / 2.0)) - 1;
    }

    public boolean canBorrow() {
        return this.keyCount > (int)(Math.ceil(this.order / 2.0)) - 1;
    }

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

    public void fromByteArray(byte[] buffer) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        DataInputStream dis = new DataInputStream(bais);

        this.order = dis.readInt();
        this.keyCount = dis.readInt();

        this.keys = new Node[this.order];
        this.children = new long[this.order + 1];

        for(int i = 0; i < this.order; i++) {
            this.children[i] = dis.readLong();

            byte[] nodeBuffer = new byte[Node.BYTES];
            dis.read(nodeBuffer);

            this.keys[i] = new Node(nodeBuffer);
        }

        this.children[this.order] = dis.readLong();
        this.next = dis.readLong();

        dis.close();
        bais.close();
    }

    public String toString() {
        return "{\n\t" + "\"address\": " + this.address + ",\n\t\"order\": " + this.order + 
                ",\n\t\"keyCount\": " + this.keyCount + ",\n\t\"keys\": " + Arrays.toString(this.keys) + 
                ",\n\t\"children\": " + Arrays.toString(children) + ",\n\t\"next\": " + this.next + "\n}";
    }

}
