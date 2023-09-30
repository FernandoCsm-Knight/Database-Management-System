package crud.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

import components.interfaces.Register;
import crud.base.BinaryArchive;
import err.JsonValidationException;

/**
 * Trash class extends BinaryArchive and represents a binary archive with
 * the ability to mark records as deleted (trash) and manage them.
 *
 * @param <T> The type of objects stored in the trash.
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class Trash<T extends Register<T>> extends BinaryArchive<T> {
    
    private long position = 0; // Position for the first data byte of the archive

    // Constructors

    /**
     * Constructor to create a Trash object with a given path and constructor.
     *
     * @param path        The path to the trash archive file.
     * @param constructor A constructor for creating objects of type T.
     * @throws IOException If there is an issue with file operations.
     */
    public Trash(String path, Constructor<T> constructor) throws IOException {
        super(path, constructor);
    }

    /**
     * Constructor to create a Trash object with a label, path, and constructor.
     *
     * @param label       A label for the trash archive.
     * @param path        The path to the trash archive file.
     * @param constructor A constructor for creating objects of type T.
     * @throws IOException If there is an issue with file operations.
     */
    public Trash(String label, String path, Constructor<T> constructor) throws IOException {
        super(label, path, constructor);
    }

    // Public methods

    /**
     * Get the label associated with the trash archive.
     *
     * @return The label of the trash archive.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Get the file path of the trash archive.
     *
     * @return The file path of the trash archive.
     */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * Get the current position within the trash archive.
     *
     * @return The current position.
     */
    public long getPosition() {
        return this.position;
    }

    /**
     * Get the length of the trash archive.
     *
     * @return The length of the trash archive.
     * @throws IOException If there is an issue with file operations.
     */
    public long length() throws IOException {
        this.file = new RandomAccessFile(this.filePath, "rw");
        long len = this.file.length();
        this.file.close();
        return len;
    }

    /**
     * Check if the trash archive is empty.
     *
     * @return True if the trash archive is empty, false otherwise.
     * @throws IOException If there is an issue with file operations.
     */
    public boolean isEmpty() throws IOException {
        this.file = new RandomAccessFile(this.filePath, "rw");
        boolean res = this.file.length() == 0;
        this.file.close();
        return res;
    }

    /**
     * Check if the trash archive is at the end of the file.
     *
     * @return True if the trash archive is at the end of the file, false otherwise.
     * @throws IOException If there is an issue with file operations.
     */
    public boolean isEOF() throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        boolean value = this.position == this.file.length();
        this.file.close();
        return value;
    }

    /**
     * Reset the position within the trash archive.
     */
    public void reset() {
        this.position = 0;
    }

    /**
     * Search for an object within the trash archive based on a key and value.
     *
     * @param key   The key to search for.
     * @param value The value to search for.
     * @return The position of the found object or -1 if not found.
     * @throws IOException If there is an issue with file operations.
     */
    public long search(String key, Object value) throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        
        long pos = 0;
        this.file.seek(pos);

        boolean lapide; // [1][valido] != [0][invalido]
        T obj = null;

        do {
            pos = this.file.getFilePointer();
            lapide = this.file.readBoolean();
            obj = this._readObj();
        } while((!lapide || obj.compare(key, value) != 0) && !this._isEOF());

        if(obj == null || obj.compare(key, value) != 0 || !lapide) 
            pos = -1;

        this.file.close();
        return pos;
    }

    /**
     * Read an object from the trash archive.
     *
     * @return The read object.
     * @throws IOException If there is an issue with file operations.
     */
    public T readObj() throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.file.seek(this.position);

        boolean lapide = false;
        int len = 0;

        while(!lapide && this.file.getFilePointer() < this.file.length()) {
            lapide = this.file.readBoolean();
            len = this.file.readInt(); 

            if(!lapide)
                this.file.skipBytes(len);
        } 

        if(this.file.getFilePointer() == this.file.length()) {
            this.file.close();
            return null;
        }

        T obj = this._readObj(len);
        
        this.position = this.file.getFilePointer();
        this.file.close();
        return obj;
    }

    /**
     * Read an object from the trash archive based on a key and value.
     *
     * @param key The key to search for.
     * @param o   The value to search for.
     * @return The read object or null if not found.
     * @throws IOException If there is an issue with file operations.
     */
    public T readObj(String key, Object o) throws IOException {
        long pos = this.search(key, o);

        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        
        T obj = null;
        if(pos != -1) {
            this.file.seek(pos);
            boolean lapide = this.file.readBoolean();

            if(lapide) 
                obj = this._readObj();
        }

        this.file.close();
        return obj;
    }

    /**
     * Create a new object within the trash archive.
     *
     * @param obj The object to create.
     * @throws IOException If there is an issue with file operations.
     */
    public void create(T obj) throws IOException {
        if(obj != null) {
            this.file = new RandomAccessFile(new File(this.filePath), "rw");
            
            if(obj.getId() == -1) 
                throw new IndexOutOfBoundsException("The ID must not be -1.");
    
            file.seek(this.file.length());
            this.file.writeBoolean(true);
            this._writeObj(obj);
            this.file.close();
        }
    }

    /**
     * Delete an object with a specific ID from the trash archive.
     *
     * @param id The ID of the object to delete.
     * @return True if the object was deleted, false otherwise.
     * @throws IOException If there is an issue with file operations.
     */
    public boolean delete(int id) throws IOException {
        long pos = this.search("id", id);
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        
        if(pos == -1) 
            return false;

        this.file.seek(pos);
        this.file.writeBoolean(false);
        this.file.close();

        return true;
    }

    /**
     * Count the number of valid (non-trashed) records in the trash archive.
     *
     * @return The count of valid records.
     * @throws IOException If there is an issue with file operations.
     */
    public int count() throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        
        int count = 0;
        this.file.seek(0);

        while(this.file.getFilePointer() < this.file.length()) {
            boolean lapide = this.file.readBoolean();
            int len = this.file.readInt();
            this.file.skipBytes(len);
            if(lapide) count++;
        }

        this.file.close();
        return count;
    }

    /**
     * Export the trash archive as a JSON file.
     *
     * @param path The path to the JSON file.
     * @throws IOException If there is an issue with file operations.
     */
    public void toJsonFile(String path) throws IOException {
        if(!path.endsWith(".json"))
            throw new JsonValidationException("The file at " + path + "is not a JSON file.");

        long tmp = this.position;

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));
        this.reset();

        bw.write("[\n");
        T obj = this.readObj();
        while(obj != null) {
            bw.write(obj.toString());
            obj = this.readObj();
            
            if(obj != null)
                bw.write(",\n");
        }

        bw.write("\n]\n");
        bw.close();
        this.position = tmp;
    }
}
