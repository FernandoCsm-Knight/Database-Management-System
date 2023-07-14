package crud.base;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

import components.interfaces.Register;
import logic.SystemSpecification;

public class BinaryArchive<T extends Register<T>> implements SystemSpecification {

    // Attributes

    private long lastPosition = 0; 
    
    public final String label; 
    public final String filePath; 
    public RandomAccessFile file; 
    public Constructor<T> constructor; 

    // Constructors

    public BinaryArchive(String path, Constructor<T> constructor) throws IOException {
        this(null, path, constructor);
    }

    public BinaryArchive(String label, String path, Constructor<T> constructor) throws IOException {
        this.label = label;
        this.filePath = path;
        this.constructor = constructor;
    }

    // Methods

    public void clear() throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.file.setLength(0);
        this.file.seek(0);
        this.file.close();
    }

    public T _readObj() throws IOException {
        this.lastPosition = this.file.getFilePointer();
        T obj = null;

        if(this.file.getFilePointer() < this.file.length()) {
            int len = this.file.readInt();
            byte[] b = new byte[len];
            
            this.file.read(b);
    
            try {
                obj = this.constructor.newInstance();
                obj.fromByteArray(b);
            } catch(Exception e) {
                System.err.println("Could not make a new instanse of " + this.constructor.getName());
                e.printStackTrace();
            }
        }

        return obj;
    }

    public T readObjFrom(RandomAccessFile f) throws IOException {
        T obj = null;

        if(f.getFilePointer() < f.length()) {
            int len = f.readInt();
            byte[] b = new byte[len];
            
            f.read(b);
    
            try {
                obj = this.constructor.newInstance();
                obj.fromByteArray(b);
            } catch(Exception e) {
                System.err.println("Could not make a new instanse of " + this.constructor.getName());
                e.printStackTrace();
            }
        }

        return obj;
    }

    public void _returnOneRegister() throws IOException {
        this.file.seek(this.lastPosition);
    }

    public void _writeObj(T obj) throws IOException {
        if(obj != null) {
            byte[] b = obj.toByteArray();

            this.file.writeInt(b.length);
            this.file.write(b);
        }
    }

    public void _writeObjs(T[] arr) throws IOException {
        for(int i = 0; i < arr.length; i++)
            this._writeObj(arr[i]);
    }

    public boolean _isEOF() throws IOException {
        return this.file.getFilePointer() >= this.file.length();
    }

    public void _resetFilePointers(BinaryArchive<T>[] arr) throws IOException {
        for(int k = 0; k < arr.length; k++)
            arr[k].file.seek(0);
    } 
}
