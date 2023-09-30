/**
 * A {@code DataBase} implementation for {@code Register} objects.
 * It extends {@code BinaryArchive} and adds additional functionality for managing data records.
 * 
 * @param <T> The type of objects stored in the database.
 * @see BinaryArchive
 * @see Register
 * @version 1.0.0
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 */
package crud.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import components.interfaces.Register;
import crud.base.BinaryArchive;
import crud.core.types.Response;
import err.EmptyFileException;
import err.JsonValidationException;

/**
 * The DataBase class extends BinaryArchive and represents a binary archive with
 * additional functionality for managing data records.
 *
 * @param <T> The type of objects stored in the database.
 */
public class DataBase<T extends Register<T>> extends BinaryArchive<T> {
    
    private long position = Integer.BYTES; // Position for the first data byte of the archive
    private int ID = 0; // ID of the last object written in the archive

    // Constructors

    /**
     * Constructor to create a DataBase object with a given path and constructor.
     *
     * @param path        The path to the database archive file.
     * @param constructor A constructor for creating objects of type T.
     * @throws IOException If there is an issue with file operations.
     */
    public DataBase(String path, Constructor<T> constructor) throws IOException {
        this(null, path, constructor);
    }

    /**
     * Constructor to create a DataBase object with a label, path, and constructor.
     *
     * @param label       A label for the database archive.
     * @param path        The path to the database archive file.
     * @param constructor A constructor for creating objects of type T.
     * @throws IOException If there is an issue with file operations.
     */
    public DataBase(String label, String path, Constructor<T> constructor) throws IOException {
        super(label, path, constructor);
        this.file = new RandomAccessFile(path, "rw");
        this.__initiateDB();
        this.file.close();
    }

    // Public methods

    /**
     * Get the label associated with the database archive.
     *
     * @return The label of the database archive.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Get the file path of the database archive.
     *
     * @return The file path of the database archive.
     */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * Get the current position within the database archive.
     *
     * @return The current position.
     */
    public long getPosition() {
        return this.position;
    }

    /**
     * Get the length of the database archive.
     *
     * @return The length of the database archive.
     * @throws IOException If there is an issue with file operations.
     */
    public long length() throws IOException {
        this.file = new RandomAccessFile(this.filePath, "rw");
        long len = this.file.length();
        this.file.close();
        return len;
    }

    /**
     * Check if the database archive is empty.
     *
     * @return True if the database archive is empty, false otherwise.
     * @throws IOException If there is an issue with file operations.
     */
    public boolean isEmpty() throws IOException {
        this.file = new RandomAccessFile(this.filePath, "rw");
        boolean res = this.file.length() == 0 || this.file.length() == Integer.BYTES;
        this.file.close();
        return res;
    }

    /**
     * Check if the database archive is at the end of the file.
     *
     * @return True if the database archive is at the end of the file, false otherwise.
     * @throws IOException If there is an issue with file operations.
     */
    public boolean isEOF() throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        boolean value = this.position == this.file.length();
        this.file.close();
        return value;
    }

    /**
     * Reset the position within the database archive.
     */
    public void reset() {
        this.position = Integer.BYTES;
    }

    /**
     * Get the last ID written to the database archive.
     *
     * @return The last ID written to the database archive.
     * @throws IOException If there is an issue with file operations.
     */
    public int getLastId() throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.file.seek(0);
        int id = this.file.readInt();
    
        this.file.close();
        return id;
    }

    /**
     * Search for an object within the database archive based on a key and value.
     *
     * @param key   The key to search for.
     * @param value The value to search for.
     * @return The position of the found object or -1 if not found.
     * @throws IOException If there is an issue with file operations.
     */
    public long search(String key, Object value) throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.__checkDefaultId();
        
        long pos = Integer.BYTES;
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
     * Restore an object with a specific ID from the database archive.
     *
     * @param id The ID of the object to restore.
     * @return A Response object indicating the success or failure of the restore operation.
     * @throws IOException If there is an issue with file operations.
     */
    public Response<T> restore(int id) throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.__checkDefaultId();

        long pos = -1;
        T obj = null;

        this.file.seek(Integer.BYTES);
        
        do {
            pos = this.file.getFilePointer();
            this.file.skipBytes(1);
            obj = this._readObj();
        } while(obj.getId() != id && !this._isEOF());

        if(obj.getId() != id) 
            return new Response<T>(false, "The object with the ID \"" + id + "\" does not exist in the file.", -1L, -1L, -1L, null);

        this.file.seek(pos);
        this.file.writeBoolean(true);
        this.file.close();
        return new Response<T>(true, "The object was restored successfully.", -1L, pos, -1L, obj);
    }

    /**
     * Read the next object from the database archive.
     *
     * @return A Response object containing the read object or an error message if the end of the file is reached.
     * @throws IOException If there is an issue with file operations.
     */
    public Response<T> readObj() throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.__checkDefaultId();

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
            return new Response<T>(false, "The end of the file was reached.", -1L, -1L, -1L, null);
        }

        T obj = this._readObj(len);
        
        Response<T> response = new Response<T>(true, "The object was readed successfully.", -1L, this.position, -1L, obj);
        this.position = this.file.getFilePointer();
        this.file.close();
        return response;
    }

    /**
     * Read an object at a specific address within the database archive.
     *
     * @param address The address at which to read the object.
     * @return The object read from the specified address or null if not found.
     * @throws IOException If there is an issue with file operations.
     */
    public T readObj(long address) throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.__checkDefaultId();

        this.file.seek(address);

        boolean lapide = this.file.readBoolean();
        if(!lapide) {
            this.file.close();
            return null;
        }

        T obj = this._readObj();
        this.file.close();
        return obj;
    }

    /**
     * Read an object from the database archive based on a key and value.
     *
     * @param key   The key to search for.
     * @param o     The value to search for.
     * @return A Response object containing the read object or an error message if the object is not found.
     * @throws IOException If there is an issue with file operations.
     */
    public Response<T> readObj(String key, Object o) throws IOException {
        long pos = this.search(key, o);
        Response<T> response = new Response<T>();
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        
        T obj = null;
        if(pos != -1) {
            this.file.seek(pos);
            boolean lapide = this.file.readBoolean();

            if(lapide) 
                obj = this._readObj();
        } else {
            response.success = false;
            response.message = "The object with the key \"" + key + "\" and value \"" + o + "\" does not exist in the file.";
        }

        if(obj != null) {
            response.success = true;
            response.message = "The object was readed successfully.";
            response.currentAddress = pos;
            response.body = obj;
        }

        this.file.close();
        return response;
    }

    /**
     * Read all objects from the database archive that match a given key and value.
     *
     * @param key   The key to search for.
     * @param o     The value to search for.
     * @return An array of objects matching the key and value.
     * @throws IOException If there is an issue with file operations.
     */
    @SuppressWarnings("unchecked")
    public T[] readAllObj(String key, Object o) throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.__checkDefaultId();

        this.file.seek(Integer.BYTES);
        ArrayList<T> list = new ArrayList<T>();

        boolean lapide = false;
        T obj = null;
        do {
            lapide = this.file.readBoolean();
            obj = this._readObj();

            if(lapide && obj.compare(key, o) == 0)
                list.add(obj);
        } while(this.file.getFilePointer() < this.file.length());

        this.file.close();
        return list.toArray((T[])Array.newInstance(obj.getClass(), list.size()));
    }

    /**
     * Update an object in the database archive by its ID with a new object.
     *
     * @param id  The ID of the object to update.
     * @param obj The new object to replace the existing one.
     * @return A Response object indicating the success or failure of the update operation.
     * @throws IOException If there is an issue with file operations.
     */
    public Response<T> update(int id, T obj) throws IOException {
        long pos = this.search("id", id);
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        Response<T> response = new Response<T>();

        if(pos == -1) {
            response.message = "The object with the ID \"" + id + "\" does not exist in the file.";
            return response;
        }

        response.success = true;
        response.oldAddress = pos;
        this.file.seek(pos);
        this.file.skipBytes(1);
        int len = this.file.readInt();

        obj.setId(id);
        byte[] b = obj.toByteArray();
 
        if(b.length <= len) {
            response.currentAddress = pos;
            response.message = "The object was updated in the same address.";
            this.file.write(b);
        } else {
            this.file.seek(pos);
            this.file.writeBoolean(false);
            
            pos = this.file.length();
            response.newAddress = pos;
            response.currentAddress = pos;
            response.message = "The object was updated in a new address.";

            this.file.seek(pos);
            this.file.writeBoolean(true);
            this.file.writeInt(b.length);
            this.file.write(b);
        }

        this.file.close();
        return response;
    }

    /**
     * Update an object in the database archive by its ID with a new value for a specific key.
     *
     * @param id    The ID of the object to update.
     * @param key   The key to update.
     * @param value The new value for the specified key.
     * @return A Response object indicating the success or failure of the update operation.
     * @throws IOException If there is an issue with file operations.
     */
    public Response<T> update(int id, String key, Object value) throws IOException {
        long pos = this.search("id", id);
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        Response<T> response = new Response<T>();
        
        if(pos == -1) {
            response.message = "The object with the ID \"" + id + "\" does not exist in the file.";
            return response;
        }

        response.success = true;
        response.oldAddress = pos;
        this.file.seek(pos);
        this.file.skipBytes(1);
        int len = this.file.readInt();

        T obj = this._readObj(len);
        obj.set(key, value);
        byte[] b = obj.toByteArray();

        if(b.length <= len) {
            response.currentAddress = pos;
            response.message = "The object was updated in the same address.";

            this.file.seek(pos);
            this.file.writeBoolean(true);
            this.file.skipBytes(Integer.BYTES);
            this.file.write(b);
        } else {
            this.file.seek(pos);
            this.file.writeBoolean(false);
            
            pos = this.file.length();
            response.newAddress = pos;
            response.currentAddress = pos;
            response.message = "The object was updated in a new address.";

            this.file.seek(pos);
            this.file.writeBoolean(true);
            this.file.writeInt(b.length);
            this.file.write(b);
        }

        this.file.close();
        return response;
    }

    /**
     * Delete an object from the database archive by its ID.
     *
     * @param id The ID of the object to delete.
     * @return True if the object was successfully deleted, false if it was not found.
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
     * Create a new object in the database archive.
     *
     * @param obj The object to create.
     * @return A Response object indicating the success of the creation and the address where the object was created.
     * @throws IOException If there is an issue with file operations.
     */
    public Response<T> create(T obj) throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.__initiateDB();

        this.file.seek(0);
        this.ID = this.file.readInt();
        
        if(obj.getId() == -1) {
            this.ID++;
            obj.setId(this.ID);
        } else if(obj.getId() <= this.ID) 
            throw new IndexOutOfBoundsException("O ID ja existe no arquivo, coloque um ID acima de " + this.ID + ".");

        long address = this.file.length();
        file.seek(address);
        this.file.writeBoolean(true);

        this._writeObj(obj);
        
        this.file.seek(0);
        this.file.writeInt(obj.getId());

        this.file.close();
        return new Response<T>(true, "The object was created in the address " + address + ".", -1L, address, -1L, obj);
    }

    /**
     * Create a new object in the database archive with an option to restore the ID.
     *
     * @param obj        The object to create.
     * @param restoreId  True if the ID should be restored (if -1), false otherwise.
     * @return A Response object indicating the success of the creation and the address where the object was created.
     * @throws IOException If there is an issue with file operations.
     */
    public Response<T> create(T obj, boolean restoreId) throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.__initiateDB();

        this.file.seek(0);
        this.ID = this.file.readInt();
        
        if(obj.getId() == -1 || restoreId) {
            this.ID++;
            obj.setId(this.ID);
        } else if(obj.getId() <= this.ID) 
            throw new IndexOutOfBoundsException("O ID ja existe no arquivo, coloque um ID acima de " + this.ID + ".");

        long address = this.file.length();
        file.seek(address);
        this.file.writeBoolean(true);

        this._writeObj(obj);
        
        this.file.seek(0);
        this.file.writeInt(obj.getId());

        this.file.close();
        return new Response<T>(true, "The object was created in the address " + address + ".", -1L, address, -1L, obj);
    }

    /**
     * Copy objects from another BinaryArchive into this one.
     *
     * @param arc The BinaryArchive to copy objects from.
     * @throws IOException If there is an issue with file operations.
     */
    public void copy(BinaryArchive<T> arc) throws IOException {
        this.clear();
        
        while(!arc._isEOF()) 
            this.__unsafeWrite(arc._readObj());
    }

    /**
     * Copy objects from a DataBase into this BinaryArchive.
     *
     * @param db The DataBase to copy objects from.
     * @throws IOException If there is an issue with file operations.
     */
    public void copy(DataBase<T> db) throws IOException {
        this.clear();
        
        while(!db.isEOF()) 
            this.__unsafeWrite(db.readObj().body);
    }

    /**
     * Export objects from the BinaryArchive to a JSON file.
     *
     * @param path The path of the JSON file to export to.
     * @throws IOException If there is an issue with file operations.
     */
    public void toJsonFile(String path) throws IOException {
        if(!path.endsWith(".json"))
            throw new JsonValidationException("The file at " + path + "is not a JSON file.");

        long tmp = this.position;

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));
        this.reset();

        bw.write("[\n");
        T obj = this.readObj().body;
        while(obj != null) {
            bw.write(obj.toString());
            obj = this.readObj().body;
            
            if(obj != null)
                bw.write(",\n");
        }

        bw.write("\n]\n");
        bw.close();
        this.position = tmp;
    }

    /**
     * Count the number of valid (not deleted) objects in the database archive.
     *
     * @return The count of valid objects.
     * @throws IOException If there is an issue with file operations.
     */
    public int count() throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        try {
            this.__checkDefaultId();
        } catch(EmptyFileException e) {
            this.file.close();
            return 0;
        }

        this.file.seek(Integer.BYTES);
        int count = 0;

        while(this.file.getFilePointer() < this.file.length()) {
            boolean lapide = this.file.readBoolean();
            int len = this.file.readInt();
            this.file.skipBytes(len);
            if(lapide) count++;
        }

        this.file.close();
        return count;
    }

    // Private methods

    /**
     * Initialize the database file by writing an initial ID if the file is empty.
     *
     * @throws IOException If there is an issue with file operations.
     */
    private void __initiateDB() throws IOException {
        if(this.file.length() == 0) {
            this.file.seek(0);
            this.file.writeInt(0);
        }
    }

    /**
     * Check the default ID from the database file and ensure it's not 0 (indicating an empty file).
     *
     * @return The ID read from the file.
     * @throws IOException If there is an issue with file operations.
     */
    private int __checkDefaultId() throws IOException {
        this.file.seek(0);
        int id = this.file.readInt();

        if(id == 0)
            throw new EmptyFileException("The file at " + this.filePath + " has no objects.");

        return id;
    }

    /**
     * Write an object to the database file without checking for existing IDs.
     *
     * @param obj The object to write.
     * @throws IOException If there is an issue with file operations.
     */
    private void __unsafeWrite(T obj) throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.__initiateDB();

        this.file.seek(0);
        this.ID = this.file.readInt();
        
        if(obj.getId() == -1) {
            this.ID++;
            obj.setId(this.ID);
        }

        file.seek(this.file.length());
        this.file.writeBoolean(true);

        this._writeObj(obj);
        
        this.file.seek(0);
        this.file.writeInt(obj.getId());

        this.file.close();
    }
    
}