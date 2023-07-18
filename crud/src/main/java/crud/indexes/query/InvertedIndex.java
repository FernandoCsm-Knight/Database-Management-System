package crud.indexes.query;

import java.io.IOException;
import java.util.ArrayList;

import crud.indexes.hash.ExtensibleHash;
import crud.indexes.types.SNode;
import crud.indexes.types.interfaces.INode;

public class InvertedIndex {

    // Attributes

    private ExtensibleHash<SNode> hash;

    // Constructors

    public InvertedIndex(String path) throws Exception {
        this.hash = new ExtensibleHash<SNode>(path, SNode.class.getConstructor(), true);
    }

    // Methods

    public String getPath() {
        return this.hash.getPath();
    }

    public void reset() throws IOException {
        this.hash.reset();
    }

    public boolean insert(String pattern, long address) throws Exception {
        String[] strs = pattern.split(" ");
        
        for(int i = 0; i < strs.length; i++) 
            if(strs[i].length() > 0) 
                this.hash.insert(SNode.fit(strs[i]), address);

        return true;
    }

    public boolean update(String pattern, long oldAddress, long newAddress) throws Exception {
        String[] strs = pattern.split(" ");
        
        for(int i = 0; i < strs.length; i++) 
            this.hash.update(SNode.fit(strs[i]), oldAddress, newAddress);

        return true;
    }

    public boolean delete(String pattern, long address) throws Exception {
        String[] strs = pattern.split(" ");
        
        for(int i = 0; i < strs.length; i++)
            this.hash.delete(SNode.fit(strs[i]), address);

        return true;
    }

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

    public void toJsonFile() throws IOException {
        this.hash.toJsonFile();
    }

    // Private methods

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
