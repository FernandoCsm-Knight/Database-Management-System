package crud.indexes.query;

import java.io.IOException;
import java.util.ArrayList;

import crud.indexes.hash.ExtensibleHash;
import crud.indexes.types.SNode;
import crud.indexes.types.interfaces.INode;

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
public class InvertedIndex {

    // Attributes

    private ExtensibleHash<SNode> hash; // Extensible Hash index

    // Constructors

    /**
     * Creates a new inverted index with the specified path.
     * 
     * @param path Path of the index
     * @throws Exception
     */
    public InvertedIndex(String path) throws Exception {
        this.hash = new ExtensibleHash<SNode>(path, SNode.class.getConstructor(), true);
    }

    // Methods

    /**
     * Returns the path of the index.
     * 
     * @return Path of the index
     */
    public String getPath() {
        return this.hash.getPath();
    }

    /**
     * Resets the index.
     * 
     * @throws IOException
     */
    public void reset() throws IOException {
        this.hash.reset();
    }

    /**
     * Inserts a new phrase in the index.
     * 
     * @param pattern Phrase to be inserted
     * @param address Address of the register that contains the sentence
     * @return True if the sentence was inserted successfully, false otherwise
     * @throws Exception
     */
    public boolean insert(String pattern, long address) throws Exception {
        String[] strs = pattern.split(" ");
        
        for(int i = 0; i < strs.length; i++) 
            if(strs[i].length() > 0) 
                this.hash.insert(SNode.fit(strs[i]), address);

        return true;
    }

    /**
     * Updates the address of a phrase in the index.
     * 
     * @param pattern Phrase to be updated
     * @param oldAddress Old address of the register that contains the sentence
     * @param newAddress New address of the register that contains the sentence
     * @return True if the sentence was updated successfully, false otherwise
     * @throws Exception 
     */
    public boolean update(String pattern, long oldAddress, long newAddress) throws Exception {
        String[] strs = pattern.split(" ");
        
        for(int i = 0; i < strs.length; i++) 
            this.hash.update(SNode.fit(strs[i]), oldAddress, newAddress);

        return true;
    }

    /**
     * Deletes a phrase from the index.
     * 
     * @param pattern Phrase to be deleted
     * @param address Address of the register that contains the sentence
     * @return True if the sentence was deleted successfully, false otherwise
     * @throws Exception
     */
    public boolean delete(String pattern, long address) throws Exception {
        String[] strs = pattern.split(" ");
        
        for(int i = 0; i < strs.length; i++)
            this.hash.delete(SNode.fit(strs[i]), address);

        return true;
    }

    /**
     * Searches for a phrase in the index.
     * 
     * @param pattern Phrase to be searched
     * @return Array of addresses of the registers that contain the sentence
     * @throws IOException
     */
    public SNode[] search(String pattern) throws IOException {
        if(pattern == null || pattern.length() == 0)
            return null;
        
        String[] strs = pattern.split(" ");
        INode<SNode>[] tmp = this.hash.readAll(SNode.fit(strs[0]));
        SNode[] keys = new SNode[tmp.length];
        for(int i = 0; i < tmp.length; i++) 
            keys[i] = (SNode)tmp[i];

        for(int i = 1; i < strs.length; i++) {
            tmp = this.hash.readAll(SNode.fit(strs[i]));
            SNode[] elements = new SNode[tmp.length];
            for(int j = 0; j < tmp.length; j++) 
                elements[j] = (SNode)tmp[j];

            keys = this.intersect(keys, elements);
        }

        return keys;
    }

    /**
     * Writes the index to a JSON file.
     * 
     * @throws IOException
     */
    public void toJsonFile() throws IOException {
        this.hash.toJsonFile();
    }

    // Private methods

    /**
     * Intersects two arrays of SNode.
     * 
     * @param elements An array of SNode to be intersected
     * @param other Other array of SNode to be intersected
     * @return An array of SNode that contains the intersection of the two arrays
     */
    private SNode[] intersect(SNode[] elements, SNode[] other) {
        ArrayList<SNode> res = new ArrayList<>();

        int i = 0, j = 0;
        while(i < elements.length && j < other.length) {
            if(((Long)elements[i].getValue()) == ((Long)other[j].getValue())) {
                res.add(elements[i]);
                i++;
                j++;
            } else if(elements[i].compareTo(other[j]) < 0) {
                i++;
            } else {
                j++;
            }
        }

        SNode[] res2 = new SNode[res.size()];
        res.toArray(res2);
        return res2;
    }
}
