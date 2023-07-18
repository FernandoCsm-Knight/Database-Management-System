package crud.indexes.hash;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

import crud.indexes.types.interfaces.INode;

public class Bucket<T extends INode<T>> {
    
    // Attributes

    private Constructor<T> constructor;
    private byte localDepth;
    private int length = 0;
    private int size = 0;
    private T[] keys;

    public final int BYTES;

    // Constructors

    @SuppressWarnings("unchecked")
    public Bucket(int length, byte localDepth, Constructor<T> constructor) {
        this.length = length;
        this.localDepth = localDepth;
        this.constructor = constructor;
        this.keys = (T[])new INode[length];

        for(int i = 0; i < this.length; i++) {
            try {
                this.keys[i] = this.constructor.newInstance();
            } catch(Exception e) {
                System.out.println("Can not make a new instance of " + this.constructor.getName() + ".");
                e.printStackTrace();
            }
        }

        this.BYTES = Byte.BYTES + // localDepth
                     Integer.BYTES + // length
                     length * this.keys[0].getBytes(); // keys
    }

    public Bucket(int length, byte[] buffer, Constructor<T> constructor) throws IOException {
        this.length = length;
        this.constructor = constructor;
        
        this.fromByteArray(buffer);

        this.BYTES = Byte.BYTES + // localDepth
                     Integer.BYTES + // length
                     length * this.keys[0].getBytes(); // keys
    }

    // Methods

    public T[] getKeys() {
        return this.keys.clone();
    }

    public boolean add(Object key, Object value) throws Exception {
        if(this.isFull())
            throw new IndexOutOfBoundsException("Cannot insert elemenet because bucket is full.");

        T node = this.constructor.newInstance();
        node.setKey(key);
        node.setValue(value);

        int i = 0;
        for(i = this.size - 1; i >= 0 && this.keys[i].compareTo(key) > 0; i--) 
            this.keys[i + 1] = this.keys[i];
        
        this.keys[i + 1] = node;
        this.size++;
        return true;
    }

    public boolean update(Object key, Object value) throws Exception {
        if(this.isEmpty())
            return false;

        T node = this.constructor.newInstance();
        node.setKey(key);
        node.setValue(value);

        boolean found = false;
        for(int i = 0; i < this.size && !found; i++) {
            found = this.keys[i].compareTo(key) == 0;
            if(found) this.keys[i] = node;
        }

        return found;
    }

    public boolean update(Object key, Object oldValue, Object newValue) throws Exception {
        if(this.isEmpty())
            return false;

        T node = this.constructor.newInstance();
        node.setKey(key);
        node.setValue(oldValue);

        boolean found = false;
        for(int i = 0; i < this.size && !found; i++) {
            found = this.keys[i].equals(node);
            if(found) {
                node.setValue(newValue);
                this.keys[i] = node;
            }
        }

        return found;
    }

    public boolean delete(Object key) throws Exception {
        if(this.isEmpty())
            return false;

        boolean found = false;
        for(int i = 0; i < this.size && !found; i++) {
            found = this.keys[i].compareTo(key) == 0;
            if(found) {
                for(int j = i; j < this.size - 1; j++) 
                    this.keys[j] = this.keys[j + 1];
                
                this.keys[this.size - 1] = this.constructor.newInstance();
                this.size--;
            }
        }

        return found;
    }

    public boolean delete(Object key, Object value) throws Exception {
        if(this.isEmpty())
            return false;

        T node = this.constructor.newInstance();
        node.setKey(key);
        node.setValue(value);

        boolean found = false;
        for(int i = 0; i < this.size && !found; i++) {
            found = this.keys[i].equals(node);
            if(found) {
                for(int j = i; j < this.size - 1; j++) 
                    this.keys[j] = this.keys[j + 1];
                
                this.keys[this.size - 1] = this.constructor.newInstance();
                this.size--;
            }
        }

        return found;
    }

    public T search(Object key) {
        for(int i = 0; i < this.size; i++) 
            if(this.keys[i].compareTo(key) == 0) 
                return this.keys[i];

        return null;
    }

    public boolean contains(Object key) {
        boolean exists = false;
        for(int i = 0; i < keys.length && !exists; i++) 
            exists = this.keys[i].compareTo(key) == 0;

        return exists;
    }

    public int getLocalDepth() {
        return this.localDepth;
    }

    public int size() {
        return this.size;
    }

    public boolean isFull() {
        return this.size == this.length;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    // Read and Write

    @SuppressWarnings("unchecked")
    public void fromByteArray(byte[] buffer) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        DataInputStream dis = new DataInputStream(bais);

        this.localDepth = dis.readByte();
        this.size = dis.readInt();
        this.keys = (T[])new INode[this.length];
    
        for(int i = 0; i < this.length; i++) {
            try {
                this.keys[i] = this.constructor.newInstance();
            } catch(Exception e) {
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

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeByte(this.localDepth);
        dos.writeInt(this.size);
        for(int i = 0; i < this.length; i++) 
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
        for(int i = 0; i < this.size; i++) {
            sb.append(this.keys[i].toString());
            if(i < this.size - 1) sb.append(", ");
        }
        return sb.append("]\n}").toString();
    }
}
