package crud.indexes.query.tmp;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

import crud.base.StructureValidation;
import logic.SystemSpecification;

/**
 * <strong> The {@code InvertedIndex} class represents an inverted index. </strong>
 * 
 * <p>
 * An inverted index is a data structure that stores a mapping from content, such as words 
 * or numbers, to its locations in a database file, or in a document or a set of documents.
 * </p>
 * 
 * <p>
 * The inverted index is used to find the addresses of the registers that contain a given
 * sentence. The sentence is split into words and each word is inserted in the index. The
 * address of the register is stored in the inverted index.
 * </p>
 * 
 * <p>
 * The inverted index is also used to update the address of a sentence. The address of the
 * register that contains the sentence is updated in the inverted index.
 * </p>
 * 
 * <p>
 * The inverted index is also used to delete a sentence. The address of the register that
 * contains the sentence is deleted from the inverted index.
 * </p>
 * 
 * @author Fernando Campos Silva Dal Maria
 * @see crud.indexes.hash.ExtensibleHash
 * @see crud.indexes.types.SNode
 * @see crud.indexes.types.interfaces.INode
 * 
 * @version 1.0.0
 */
public class InvertedIndex implements SystemSpecification {

    // Classes

    static class Node {
        public static final int BYTES = Long.BYTES * 2;
        public long address = -1;
        public long next = -1;

        public Node(long address, long next) {
            this.address = address;
            this.next = next;
        }

        public Node(byte[] byffer) throws IOException {
            this.fromByteArray(byffer);
        }

        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeLong(this.address);
            dos.writeLong(this.next);

            byte[] buffer = baos.toByteArray();
            dos.close();
            baos.close();
            return buffer;
        }

        public void fromByteArray(byte[] buffer) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            DataInputStream dis = new DataInputStream(bais);

            this.address = dis.readLong();
            this.next = dis.readLong();

            dis.close();
            bais.close();
        }

        @Override
        public String toString() {
            return "{ \"address\": " + this.address + ", \"next\": " + this.next + " }";
        }
    }

    // Attributes

    private final String path;
    private final TermList terms;
    private RandomAccessFile file;

    // Constructors

    public InvertedIndex(String path) throws IOException {
        StructureValidation.createIndexesDirectory();

        File file = new File(INDEXES_FILES_DIRECTORY);
        String[] filePaths = file.list();

        String termListPath = UUID.randomUUID().toString() + path;
        for(int i = 0; i < filePaths.length; i++) 
            if(filePaths[i].endsWith(path) && !filePaths[i].equals(path))
                termListPath = filePaths[i];

        this.terms = new TermList(termListPath);
        this.path = INDEXES_FILES_DIRECTORY + path;
        this.init();
    }

    public void insert(String pattern, long value) throws IOException {
        String[] strs = this.filter(pattern.trim().toLowerCase().split(" "));
        this.file = new RandomAccessFile(this.path, "rw");

        long address = -1;
        for(int i = 0; i < strs.length; i++) {
            Term term = this.terms.read(strs[i]);
            if(term == null) {
                address = this.file.length();
                this.file.seek(address);
                this.file.writeBoolean(true);
                this.file.write(new Node(value, -1L).toByteArray());
                this.terms.create(strs[i], address);
            } else {
                address = term.address;
    
                long prev = -1;
                Node curr = null;
                boolean lapide = true;
                while(address != -1) {
                    prev = address;
                    this.file.seek(address);
                    byte[] buffer = new byte[Node.BYTES];
                    lapide = this.file.readBoolean();
                    this.file.read(buffer);
                    curr = new Node(buffer);
                    address = curr.next;
                }
                
                this.file.seek(prev);
                curr.next = this.file.length();
                this.file.writeBoolean(lapide);
                this.file.write(curr.toByteArray());

                this.file.seek(curr.next);
                this.file.writeBoolean(true);
                this.file.write(new Node(value, -1L).toByteArray());
                this.terms.create(strs[i]);
            }            
        }

        this.file.close();
    }

    public boolean delete(String pattern, long value) throws IOException {
        String[] strs = this.filter(pattern.trim().toLowerCase().split(" "));
        this.file = new RandomAccessFile(this.path, "rw");

        long address = -1;
        for(int i = 0; i < strs.length; i++) {
            Term term = this.terms.read(strs[i]);
            if(term != null) {
                address = term.address;
    
                long prev = -1;
                Node curr = null;
                do {
                    prev = address;
                    this.file.seek(address);
                    byte[] buffer = new byte[Node.BYTES];
                    this.file.skipBytes(1);
                    this.file.read(buffer);
                    curr = new Node(buffer);
                    address = curr.next;
                } while(address != -1 && curr.address != value);

                if(curr.address == value) {
                    this.file.seek(prev);
                    this.file.writeBoolean(false);
                    this.file.write(curr.toByteArray());
                    this.terms.delete(strs[i]);
                }
            }
        }

        this.file.close();
        return true;
    }

    public void update(String pattern, long oldAddress, long newAddress) throws IOException {
        String[] strs = this.filter(pattern.trim().toLowerCase().split(" "));
        this.file = new RandomAccessFile(this.path, "rw");

        long address = -1;
        for(int i = 0; i < strs.length; i++) {
            Term term = this.terms.read(strs[i]);
            if(term != null) {
                address = term.address;
    
                long prev = -1;
                Node curr = null;
                boolean lapide = true;
                do {
                    prev = address;
                    this.file.seek(address);
                    byte[] buffer = new byte[Node.BYTES];
                    lapide = this.file.readBoolean();
                    this.file.read(buffer);
                    curr = new Node(buffer);
                    address = curr.next;
                } while(address != -1 && curr.address != oldAddress);

                if(curr.address == oldAddress) {
                    curr.address = newAddress;

                    this.file.seek(prev);
                    this.file.writeBoolean(lapide);
                    this.file.write(curr.toByteArray());
                }
            }
        }

        this.file.close();
    }

    public long[] read(String pattern) throws IOException {
        String[] strs = pattern.trim().toLowerCase().split(" ");
        this.file = new RandomAccessFile(this.path, "rw");

        int k = 0;
        ArrayList<HashSet<Long>> addresses = new ArrayList<>();
        for(int i = 0; i < strs.length; i++) {
            Term term = this.terms.read(strs[i]);
            if(term != null) {
                addresses.add(new HashSet<Long>());
                long address = term.address;
                boolean lapide = true;

                while(address != -1) {
                    this.file.seek(address);
                    byte[] buffer = new byte[Node.BYTES];
                    lapide = this.file.readBoolean();
                    this.file.read(buffer);
                    Node curr = new Node(buffer);
                    if(lapide) addresses.get(k).add(curr.address);
                    address = curr.next;
                }

                k++;
            }
        }

        this.file.close();

        ArrayList<long[]> res = new ArrayList<>();
        for(int i = 0; i < addresses.size(); i++) {
            long[] arr = new long[addresses.get(i).size()];
            int j = 0;
            for(Long l : addresses.get(i)) 
                arr[j++] = l;
            res.add(arr);
        }

        return this.intersect(res);
    }

    /**
     * Writes the index to a JSON file.
     * 
     * @throws IOException
     */
    public void toJsonFile() throws IOException {
        StructureValidation.createJSONIndexDirectory();
        String[] strs = this.path.split("/");
        ArrayList<String> unique = this.terms.getAllTerms();
        this.file = new RandomAccessFile(this.path, "rw");

        StringBuffer sb = new StringBuffer("[\n");
        for(int i = 0; i < unique.size(); i++) {
            sb.append("{ \"").append(unique.get(i)).append("\": [\n");

            long address = this.terms.read(unique.get(i)).address;

            Node curr = null;
            boolean lapide = true;
            while(address != -1) {
                this.file.seek(address);
                byte[] buffer = new byte[Node.BYTES];
                lapide = this.file.readBoolean();
                this.file.read(buffer);
                curr = new Node(buffer);
                address = curr.next;

                if(lapide) {
                    sb.append("\t").append(curr);
                    if(address != -1) sb.append(",\n");
                }
            }

            sb.append("\n]}");
            if(i < unique.size() - 1) sb.append(",\n");
        }
        
        sb.append("\n]");
        this.file.close();

        BufferedWriter bw = new BufferedWriter(new FileWriter(JSON_INDEXES_DIRECTORY + strs[strs.length - 1].replace(".db", ".json")));
        bw.write(sb.toString());
        bw.close();

        bw = new BufferedWriter(new FileWriter(JSON_INDEXES_DIRECTORY + "_Terms_" + strs[strs.length - 1].replace(".db", ".json")));
        bw.write(this.terms.toString());
        bw.close();
    }

    public void clear() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.setLength(0);
        this.file.close();

        this.terms.clear();
    }

    // Private methods

    private void init() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.close();
    }

    /**
     * Intersects two arrays of SNode.
     * 
     * @param elements An array of SNode to be intersected
     * @param other Other array of SNode to be intersected
     * @return An array of SNode that contains the intersection of the two arrays
     */
    public long[] intersect(ArrayList<long[]> arr) {
        if(arr.size() == 0)
            return new long[0];

        ArrayList<Long> res = new ArrayList<>();

        long[] elements = arr.get(0);
        for(int k = 1; k < arr.size(); k++) {
            long[] other = arr.get(k);
            for(int i = 0; i < elements.length; i++) {
                for(int j = 0; j < other.length; j++) {
                    if (elements[i] != -1 && other[j] != -1 && elements[i] == other[j]) {
                        res.add(elements[i]);
                    } 
                }
            }

            elements = new long[res.size()];
            for(int i = 0; i < res.size(); i++) 
                elements[i] = (long)res.get(i);

            res.clear();
        }

        return elements;
    }

    /**
     * Filter a string array by removing the words that are not apropriate. 
     * 
     * @param strs The string array to be filtered.
     * @return The filtered string array.
     */
    public String[] filter(String[] strs) {
        HashSet<String> hash = new HashSet<>();

        hash.add("a");
        hash.add("o");
        hash.add("as");
        hash.add("os");
        hash.add("lo");
        hash.add("la");
        hash.add("los");
        hash.add("las");
        hash.add("el");
        hash.add("de");
        hash.add("da");
        hash.add("dos");
        hash.add("das");
        hash.add("do");
        hash.add("um");
        hash.add("uma");
        hash.add("uns");
        hash.add("umas");
        hash.add("em");
        hash.add("no");
        hash.add("na");
        hash.add("nos");
        hash.add("nas");
        hash.add("un");
        hash.add("una");
        hash.add("an");
        hash.add("is");
        hash.add("the");
        hash.add("it");
        hash.add("its");
        hash.add("of");
        hash.add("in");
        hash.add("on");
        hash.add("at");
        hash.add("to");
        hash.add("for");
        hash.add("and");
        hash.add("or");
        hash.add("e");
        hash.add("y");
        hash.add("no");
        hash.add("si");
        hash.add("hay");
        hash.add("sí");
        hash.add("it's");

        ArrayList<String> arr = new ArrayList<>();

        for(int i = 0; i < strs.length; i++) 
            if(!hash.contains(strs[i]) && (strs[i].length() >= 3 || strs.length <= 2))
                arr.add(strs[i]);

        while(arr.size() > 1) 
            arr.remove(new Random().nextInt(0, arr.size()));

        String[] newStrs = new String[arr.size()];
        return arr.toArray(newStrs);
    }
}
