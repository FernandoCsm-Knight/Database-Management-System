package crud.base;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

import components.interfaces.Register;
import logic.SystemSpecification;

/**
 * The class {@code BinaryArchive} represents a binary archive for a {@link crud.karnel.DataBase}.
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 * 
 * @see {@link components.interfaces.Register}
 */
public class BinaryArchive<T extends Register<T>> extends SystemSpecification {

    // Attributes

    private long lastPosition = 0; // used to keep track of the last position of the file pointer
    
    public final String label; // used to identify the archive
    public final String filePath; // used to identify the file path
    public RandomAccessFile file; // used to access the file
    public Constructor<T> constructor; // used to create a new instance of the register

    // Constructors

    /**
     * Constructs a new {@code BinaryArchive} with the given file path and constructor.
     * @param path the file path of the archive.
     * @param constructor the constructor of the register.
     * @throws IOException if an I/O error occurs.
     * 
     * @see {@link java.io.IOException}
     */
    public BinaryArchive(String path, Constructor<T> constructor) throws IOException {
        this(null, path, constructor);
    }

    /**
     * Constructs a new {@code BinaryArchive} with the given label, file path and constructor.
     * @param label the label of the archive.
     * @param path the file path of the archive.
     * @param constructor the constructor of the register.
     * @throws IOException if an I/O error occurs.
     * 
     * @see {@link java.io.IOException}
     */
    public BinaryArchive(String label, String path, Constructor<T> constructor) throws IOException {
        this.label = label;
        this.filePath = path;
        this.constructor = constructor;
    }

    // Methods

    /**
     * Clear the archive, setting it`s length to 0.
     * @throws IOException if an I/O error occurs.
     * 
     * @see {@link java.io.IOException}
     */
    public void clear() throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.file.setLength(0);
        this.file.seek(0);
        this.file.close();
    }

    /**
     * Read a binary array and converts it to a register.
     * @return the new register from type {@code T}.
     * @throws IOException if an I/O error occurs.
     * 
     * @see {@link java.io.IOException}
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
     * Read a binary array from the file settend in the {@code RandomAccessFile} and converts it to a register.
     * @param f the {@code RandomAccessFile} object to read from.
     * @return the new register from type {@code T}.
     * @throws IOException if an I/O error occurs.
     * 
     * @see {@link java.io.RandomAccessFile}
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
     * Return the file pointer to the start of the last register readed.
     * @throws IOException if an I/O error occurs.
     * 
     * @see {@link java.io.IOException}
     */
    public void _returnOneRegister() throws IOException {
        this.file.seek(this.lastPosition);
    }

    /**
     * Write a register to a binary file.
     * @param obj the register to be written.
     * @throws IOException if an I/O error occurs.
     * 
     * @see {@link java.io.IOException}
     */
    public void _writeObj(T obj) throws IOException {
        if(obj != null) {
            byte[] b = obj.toByteArray();

            this.file.writeInt(b.length);
            this.file.write(b);
        }
    }
    
    /**
     * Write a register array to a binary file.
     * @param arr the register array to be written.
     * @throws IOException if an I/O error occurs.
     * 
     * @see {@link java.io.IOException}
     */
    public void _writeObjs(T[] arr) throws IOException {
        for(int i = 0; i < arr.length; i++)
            this._writeObj(arr[i]);
    }

    /**
     * Tests if the file pointer is at the end of the file.
     * @return {@code true} if the file pointer is at the end of the file, {@code false} otherwise.
     * 
     * @see {@link java.io.IOException}
     */
    public boolean _isEOF() throws IOException {
        return this.file.getFilePointer() >= this.file.length();
    }

        /**
     * Changes all pointers of a given array of files to the beginning of the file.
     * @param arr the array of files.
     * @throws IOException if an I/O error occurs.
     */
    public void _resetFilePointers(BinaryArchive<T>[] arr) throws IOException {
        for(int k = 0; k < arr.length; k++)
            arr[k].file.seek(0);
    } 
}
