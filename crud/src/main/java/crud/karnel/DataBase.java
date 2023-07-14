package crud.karnel;

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
import err.EmptyFileException;
import err.JsonValidationException;

public class DataBase<T extends Register<T>> extends BinaryArchive<T> {
    
    private long position = Integer.BYTES;
    private int ID = 0;

    public DataBase(String path, Constructor<T> constructor) throws IOException {
        this(null, path, constructor);
    }

    public DataBase(String label, String path, Constructor<T> constructor) throws IOException {
        super(label, path, constructor);
    }

    public String getLabel() {
        return this.label;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public long getPosition() {
        return this.position;
    }

    public long length() throws IOException {
        this.file = new RandomAccessFile(this.filePath, "rw");
        long len = this.file.length();
        this.file.close();
        return len;
    }

    public boolean isEmpty() throws IOException {
        this.file = new RandomAccessFile(this.filePath, "rw");
        boolean res = this.file.length() == 0 || this.file.length() == Integer.BYTES;
        this.file.close();
        return res;
    }

    public boolean isEOF() throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        boolean value = this.position == this.file.length();
        this.file.close();
        return value;
    }

    public void reset() {
        this.position = Integer.BYTES;
    }

    private void __initiateDB() throws IOException {
        if(this.file.length() == 0) {
            this.file.seek(0);
            this.file.writeInt(0);
        }
    }

    private int __checkDefaultId() throws IOException {
        this.file.seek(0);
        int id = this.file.readInt();

        if(id == 0)
            throw new EmptyFileException("The file at " + this.filePath + " has no objects.");

        return id;
    }

    public int getLastId() throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.file.seek(0);
        int id = this.file.readInt();
    
        this.file.close();
        return id;
    }

    public long search(String key, Object value) throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.__checkDefaultId();
        
        long pos = Integer.BYTES;
        this.file.seek(pos);

        boolean lapide;

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

    public T readObj() throws IOException {
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

        if(this.file.getFilePointer() == this.file.length()) 
            return null;

        byte[] b = new byte[len];
        this.file.read(b);
        
        T obj = null;
        try {
            obj = this.constructor.newInstance();
            obj.fromByteArray(b);
        } catch(Exception e) {
            System.err.println("Could not make a new instanse of " + this.constructor.getName());
            e.printStackTrace();
        }
        
        this.position = this.file.getFilePointer();
        this.file.close();
        return obj;
    }

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

    public boolean update(int id, T obj) throws IOException {
        long pos = this.search("id", id);
        this.file = new RandomAccessFile(new File(this.filePath), "rw");

        if(pos == -1) 
            return false;

        this.file.seek(pos);
        this.file.skipBytes(1);
        int len = this.file.readInt();

        obj.setId(id);
        byte[] b = obj.toByteArray();
 
        if(b.length <= len) {
            this.file.write(b);
        } else {
            this.file.seek(pos);
            this.file.writeBoolean(false);
            this.__unsafeWrite(obj);
        }

        this.file.close();
        return true;
    }

    public boolean update(int id, String key, Object value) throws IOException {
        long pos = this.search("id", id);
        this.file = new RandomAccessFile(new File(this.filePath), "rw");

        if(pos == -1) 
            return false;

        this.file.seek(pos);
        this.file.skipBytes(1);
        int len = this.file.readInt();

        this.file.seek(pos);
        this.file.skipBytes(1);

        T obj = this._readObj();
        obj.set(key, value);
        byte[] b = obj.toByteArray();

        if(b.length <= len) {
            this.file.seek(pos);
            this.file.writeBoolean(true);
            this.file.writeInt(b.length);
            this.file.write(b);
        } else {
            this.file.seek(pos);
            this.file.writeBoolean(false);
            this.__unsafeWrite(obj);
        }

        this.file.close();
        return true;
    }

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

    public void write(T obj) throws IOException {
        this.file = new RandomAccessFile(new File(this.filePath), "rw");
        this.__initiateDB();

        this.file.seek(0);
        this.ID = this.file.readInt();
  
        obj = obj.clone();
        
        if(obj.getId() == -1) {
            this.ID++;
            obj.setId(this.ID);
        }

        if(obj.getId() < this.ID) 
            throw new IndexOutOfBoundsException("O ID ja existe no arquivo, coloque um ID acima de " + this.ID + ".");

        file.seek(this.file.length());
        this.file.writeBoolean(true);

        this._writeObj(obj);
        
        this.file.seek(0);
        this.file.writeInt(obj.getId());

        this.file.close();
    }

    public void copy(BinaryArchive<T> arc) throws IOException {
        this.clear();
        
        while(!arc._isEOF()) 
            this.__unsafeWrite(arc._readObj());
    }

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