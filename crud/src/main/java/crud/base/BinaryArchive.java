package crud.base;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

import components.interfaces.Register;
import logic.SystemSpecification;

/**
 * A generic binary archive for reading and writing objects of a specified type.
 *
 * @param <T> The type of objects to be stored in the archive.
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class BinaryArchive<T extends Register<T>> implements SystemSpecification {

    // Attributes

    /**
     * The last read/write position in the binary archive file.
     */
    private long lastPosition = 0; 
    
    public final String label;  // Label for the archive
    public final String filePath; // Path for the archive
    public RandomAccessFile file; // File object for the archive
    public Constructor<T> constructor; // Constructor for the objects to be stored in the archive

    // Constructors

    /**
     * Creates a BinaryArchive with a specified file path and constructor.
     *
     * @param path        The file path where the binary archive is stored.
     * @param constructor The constructor for creating objects of type T.
     * @throws IOException If an I/O error occurs while creating the archive.
     */
    public BinaryArchive(String path, Constructor<T> constructor) throws IOException {
        this(null, path, constructor);
    }

    /**
     * Creates a BinaryArchive with a specified label, file path, and constructor.
     *
     * @param label       The label associated with the binary archive.
     * @param path        The file path where the binary archive is stored.
     * @param constructor The constructor for creating objects of type T.
     * @throws IOException If an I/O error occurs while creating the archive.
     */
    public BinaryArchive(String label, String path, Constructor<T> constructor) throws IOException {
        this.label = label;
        this.filePath = path;
        this.constructor = constructor;
    }

    // Methods

    /**
     * Clears the contents of the binary archive.
     *
     * @throws IOException If an I/O error occurs while clearing the archive.
     */
    public void clear() throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.file.setLength(0);
        this.file.seek(0);
        this.file.close();
    }

    /**
     * Reads an object of type T from the binary archive.
     *
     * @return The read object or null if the end of the archive is reached.
     * @throws IOException If an I/O error occurs while reading the object.
     */
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

    /**
     * Reads an object of type T with a specified length from the binary archive.
     *
     * @param len The length of the object data to read.
     * @return The read object or null if the end of the archive is reached.
     * @throws IOException If an I/O error occurs while reading the object.
     */
    public T _readObj(int len) throws IOException {
        this.lastPosition = this.file.getFilePointer();
        T obj = null;

        if(this.file.getFilePointer() < this.file.length()) {
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

    /**
     * Reads an object of type T from a specified RandomAccessFile.
     *
     * @param f The RandomAccessFile from which to read the object.
     * @return The read object or null if the end of the file is reached.
     * @throws IOException If an I/O error occurs while reading the object.
     */
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

    /**
     * Sets the file pointer to the last read/write position.
     */
    public void _returnOneRegister() throws IOException {
        this.file.seek(this.lastPosition);
    }

    /**
     * Writes an object of type T to the binary archive.
     *
     * @param obj The object to write to the archive.
     * @throws IOException If an I/O error occurs while writing the object.
     */
    public void _writeObj(T obj) throws IOException {
        if(obj != null) {
            byte[] b = obj.toByteArray();

            this.file.writeInt(b.length);
            this.file.write(b);
        }
    }

    /**
     * Writes an array of objects of type T to the binary archive.
     *
     * @param arr The array of objects to write to the archive.
     * @throws IOException If an I/O error occurs while writing the objects.
     */
    public void _writeObjs(T[] arr) throws IOException {
        for(int i = 0; i < arr.length; i++)
            this._writeObj(arr[i]);
    }

    /**
     * Checks if the end of the binary archive file is reached.
     *
     * @return True if the end of the file is reached, false otherwise.
     * @throws IOException If an I/O error occurs while checking the end of the file.
     */
    public boolean _isEOF() throws IOException {
        return this.file.getFilePointer() == this.file.length();
    }

    /**
     * Resets the file pointers of an array of BinaryArchives to the beginning of their respective files.
     *
     * @param arr The array of BinaryArchives to reset.
     * @throws IOException If an I/O error occurs while resetting the file pointers.
     */
    public void _resetFilePointers(BinaryArchive<T>[] arr) throws IOException {
        for(int k = 0; k < arr.length; k++)
            arr[k].file.seek(0);
    } 
}
