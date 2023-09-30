package crud.indexes.query.tmp;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import logic.SystemSpecification;

public class TermList implements SystemSpecification, AutoCloseable {
    
    // Attributes

    private RandomAccessFile file;
    private final String path;
    private int length;

    // Constructors
    
    public TermList(String path) throws IOException {
        this.path = INDEXES_FILES_DIRECTORY + path;
        this.file = new RandomAccessFile(this.path, "rw");
        this.length = 0;
        this.init();
    }

    // Methods

    private void init() throws IOException {
        if(this.file.length() == 0) {
            this.file.seek(0);
            this.file.writeInt(this.length);
        }
    }

    private long search(String term) throws IOException {
        this.file.seek(Integer.BYTES);

        long address = -1;
        while(address == -1 && this.file.getFilePointer() < this.file.length()) {
            byte[] buffer = new byte[Term.BYTES];
            this.file.read(buffer);
            Term t = new Term(buffer);

            if(t.contains(term)) 
                address = this.file.getFilePointer() - Term.BYTES;
        }

        return address;
    }

    public boolean contains(String term) throws IOException {
        return this.search(term) != -1;
    }

    public Term read(String term) throws IOException {
        long pos = this.search(term);

        if(pos == -1) 
            return null;

        this.file.seek(pos);
        byte[] buffer = new byte[Term.BYTES];
        this.file.read(buffer);
        Term t = new Term(buffer);
        return t;
    }

    public void create(String term) throws IOException {
        long pos = this.search(term);
        if(pos == -1) 
            throw new IndexOutOfBoundsException("The does't already exists, you need to provide an address.");

        this.create(term, -1);
    }

    public void create(String term, long address) throws IOException {
        long pos = this.search(term);

        if(pos == -1) {
            this.file.seek(this.file.length());
            this.file.write(new Term(term, address).toByteArray());
            
            this.length++;
            this.file.seek(0);
            this.file.writeInt(this.length);
        } else {
            this.file.seek(pos);
            byte[] buffer = new byte[Term.BYTES];
            this.file.read(buffer);
            Term t = new Term(buffer);
            t.count++;
            this.file.seek(pos);
            this.file.write(t.toByteArray());
        }
    }

    public void update(String term, long address) throws IOException {
        long pos = this.search(term);
        
        if(pos == -1) 
            throw new IndexOutOfBoundsException("The term does not exist.");
        
        this.file.seek(pos);
        byte[] buffer = new byte[Term.BYTES];
        this.file.read(buffer);
        Term t = new Term(buffer);
        t.count++;
        this.file.seek(pos);
        this.file.write(t.toByteArray());
    }

    public void delete(String term) throws IOException {
        long pos = this.search(term);

        if(pos == -1 || this.read(term).count == 0) 
            throw new IndexOutOfBoundsException("The term does not exist.");

        this.file.seek(pos);
        byte[] buffer = new byte[Term.BYTES];
        this.file.read(buffer);
        Term t = new Term(buffer);
        if(t.count > 0) {
            t.count--;
            this.file.seek(pos);
            this.file.write(t.toByteArray());
        } else {
            this.length--;
            this.file.seek(0);
            this.file.writeInt(this.length);
        }

    }

    public ArrayList<String> getAllTerms() throws IOException {
        ArrayList<String> terms = new ArrayList<String>();

        this.file.seek(Integer.BYTES);

        while(this.file.getFilePointer() < this.file.length()) {
            byte[] buffer = new byte[Term.BYTES];
            this.file.read(buffer);
            Term t = new Term(buffer);
            terms.add(t.getTerm().trim());
        }

        return terms;
    }

    public void clear() throws IOException {
        this.file.setLength(0);
        this.init();
    }

    @Override
    public void close() throws IOException {
        this.file.close();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("[\n");

        try {
            this.file.seek(Integer.BYTES);
    
            while(this.file.getFilePointer() < this.file.length()) {
                byte[] buffer = new byte[Term.BYTES];
                this.file.read(buffer);
                Term t = new Term(buffer);
                sb.append("\t").append(t);
                if(this.file.getFilePointer() < this.file.length()) sb.append(",\n");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        return sb.append("\n]").toString();
    }
}
