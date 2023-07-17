package crud.indexes.hash;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import logic.SystemSpecification;

public class Directory implements SystemSpecification {
    
    // Attributes

    private String path;
    private long[] directory;
    private byte globalDepth = 0;
    private RandomAccessFile file;

    // Constructor

    public Directory(String path) throws IOException {
        if(!path.endsWith(".db"))
            throw new IllegalArgumentException("Path to a extensible hash index must end with \".db\".");


        this.path = INDEXES_FILES_DIRECTORY + path;

        this.file = new RandomAccessFile(this.path, "rw");

        if(this.file.length() == 0) {
            this.directory = new long[1];            
            this.file.write(this.toByteArray());
        } else {
            byte[] buffer = new byte[(int)this.file.length()];
            this.file.read(buffer);
            
            this.fromByteArray(buffer);
        }

        this.file.close();
    }

    public String getPath() {
        return this.path;
    }

    public byte getGlobalDepth() {
        return this.globalDepth;
    }

    public long[] getDirectory() {
        return this.directory.clone();
    }

    public long getAddress(Object key) {
        return this.directory[this.hash(key)];
    }

    public void setAddress(int idx, long address) throws IOException {
        this.directory[idx] = address;

        this.file = new RandomAccessFile(this.path, "rw");
        this.file.seek(Byte.BYTES + Long.BYTES * idx);
        this.file.writeLong(address);
        this.file.close();
    }

    public void doubleSize() throws IOException {
        this.globalDepth++;
        long[] newDirectory = new long[1 << this.globalDepth];

        for(int i = 0; i < this.directory.length; i++) {
            newDirectory[i] = this.directory[i];
            newDirectory[i + this.directory.length] = this.directory[i];
        }
        
        this.directory = newDirectory;

        this.file = new RandomAccessFile(this.path, "rw");
        this.file.seek(0);
        this.file.write(this.toByteArray());
        this.file.close();
    }

    public void reset() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.setLength(0);

        this.globalDepth = 0;
        this.directory = new long[1];
        this.file.write(this.toByteArray());

        this.file.close();
    }

    public void fromByteArray(byte[] buffer) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        DataInputStream dis = new DataInputStream(bais);

        this.globalDepth = dis.readByte();
        this.directory = new long[1 << this.globalDepth];

        for(int i = 0; i < this.directory.length; i++) 
            this.directory[i] = dis.readLong();
        
        dis.close();
        bais.close();
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeByte(this.globalDepth);
        for(int i = 0; i < this.directory.length; i++) 
            dos.writeLong(this.directory[i]);

        byte[] buffer = baos.toByteArray();
        dos.close();
        baos.close();
        return buffer;
    }

    private int hash(Object key) {
        return key.hashCode() & ((1 << this.globalDepth) - 1);
    }

    public int reHash(Object key, int depth) {
        return key.hashCode() & ((1 << depth) - 1);
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
}
