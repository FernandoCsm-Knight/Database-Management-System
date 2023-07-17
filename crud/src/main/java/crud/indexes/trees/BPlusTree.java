package crud.indexes.trees;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

import crud.base.StructureValidation;
import crud.indexes.types.interfaces.INode;
import logic.SystemSpecification;

public class BPlusTree<T extends INode<T>> implements SystemSpecification {

    // Attributes

    private static final int MIN_ORDER = 3;
    
    private int order;
    private String path;
    private long root = -1;
    private RandomAccessFile file;
    private Constructor<T> constructor;

    // Delete auxiliar variables

    private long addressToRewrite = -1;
    private int indexToRewrite = -1;

    public final int PAGE_BYTES;

    // Constructors

    public BPlusTree(int order, String path, Constructor<T> constructor) {
        if(!path.endsWith(".db"))
            throw new IllegalArgumentException("The B+ Tree name must ends with .db");

        if(order < MIN_ORDER) 
            throw new IllegalArgumentException("Order must be greater than " + MIN_ORDER);

        this.order = order;
        this.constructor = constructor;
        this.path = INDEXES_FILES_DIRECTORY + path;
        this.PAGE_BYTES = new Page<T>(order, constructor).BYTES;
    }

    // Initialize Methods

    public boolean init() {
        if(this.length() >= 8)
            throw new IllegalAccessError("The archive \"" + this.path + "\" is not empty. If you want to initialize it anyway use the reset() method.");

        try {
            this.updateRoot(Long.BYTES);
            this.writePage(new Page<T>(this.order, constructor), Long.BYTES);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void reset() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.setLength(0);
        this.file.close();
        
        this.init();
    }

    // Public Methods

    public long length() {
        long len = -1;
        try {
            this.file = new RandomAccessFile(this.path, "rw");
            len = this.file.length();
            this.file.close();
        } catch(IOException e) {
            System.out.println("Is was not possible to open the B+ tree file");
            e.printStackTrace();
        }
        return len;
    }

    public T search(Object key) throws IOException {
        return this.search(key, this.readPage(this.root));
    }

    public boolean update(Object key, Object value) throws Exception {
        T node = constructor.newInstance();
        node.setKey(key);
        node.setValue(value);

        return this.update(node, this.readPage(this.root));
    }

    public void insert(Object key, Object value) throws Exception {
        T node = constructor.newInstance();
        node.setKey(key);
        node.setValue(value);

        Page<T> newPage = this.insert(node, this.readPage(this.root));

        if(newPage != null) {
            Page<T> newRoot = new Page<T>(this.order, this.constructor);
            newRoot.children[0] = this.root;
            newRoot.keys[0] = newPage.keys[0].clone();
            newRoot.children[1] = newPage.address;
            newRoot.keyCount++;

            if(newPage.children[0] != -1) {
                for(int i = 0; i < newPage.keyCount; i++) {
                    newPage.keys[i] = newPage.keys[i + 1];
                }

                newPage.keyCount--;
            }

            this.writePage(newPage, newPage.address);
            
            this.updateRoot(this.length());
            this.writePage(newRoot, this.root);
        }
    }

    public boolean delete(Object key) throws IOException {
        Page<T> newRoot = this.delete(key, this.readPage(this.root));

        if(newRoot != null) {
            if(newRoot.isUnderflow() && newRoot.children[0] != -1 && newRoot.keyCount == 0) {
                this.updateRoot(newRoot.children[0]);
                this.writePage(this.readPage(this.root), this.root);
            } else if(newRoot.children[0] == -1 && newRoot.address == this.root) {
                this.writePage(newRoot, this.root);
            }
        }

        if(this.addressToRewrite != -1) {
            int i = this.indexToRewrite;
            Page<T> pageToRewrite = this.readPage(this.addressToRewrite);

            pageToRewrite.keys[i] = minKey(this.readPage(pageToRewrite.children[i + 1]));
            this.writePage(pageToRewrite, pageToRewrite.address);

            this.addressToRewrite = -1;
            this.indexToRewrite = -1;
        }

        return newRoot != null;
    }

    public void toJsonFile() throws IOException {
        StructureValidation.createJSONIndexDirectory();

        String[] strs = this.path.split("/");

        StringBuffer sb = new StringBuffer("[\n{\n\t\"root\": " + root + ",\n\t\"order\": " + this.order + "\n},\n");
        toJsonFile(sb, this.readPage(this.root));

        BufferedWriter br = new BufferedWriter(new FileWriter(JSON_INDEXES_DIRECTORY + strs[strs.length - 1].replace(".db", ".json")));
        br.write(sb.toString().substring(0, sb.length() - 2) + "\n]");
        br.close();
    }

    //Private Methods

    private T search(Object key, Page<T> curr) throws IOException {
        if(curr.children[0] == -1) {
            for(int i = 0; i < curr.keyCount; i++) 
                if(curr.keys[i].compareTo(key) == 0)
                    return curr.keys[i];

            return null;
        } else {
            int i = 0;
            while(i < curr.keyCount && curr.keys[i].compareTo(key) <= 0) {
                i++;
            }

            return this.search(key, this.readPage(curr.children[i]));
        }
    }

    public boolean update(T key, Page<T> curr) throws IOException {
        if(curr.children[0] == -1) {
            for(int i = 0; i < curr.keyCount; i++) {
                if(curr.keys[i].compareTo(key) == 0) {
                    curr.keys[i] = key;
                    this.writePage(curr, curr.address);
                    return true;
                }
            }

            return false;
        } else {
            int i = 0;
            while(i < curr.keyCount && curr.keys[i].compareTo(key) <= 0) {
                i++;
            }
            
            return this.update(key, this.readPage(curr.children[i]));
        }
    }

    private Page<T> deleteChild(Object key, Page<T> curr) {
        Page<T> res = null;
        int idx = -1;
        for(int i = 0; i < curr.keyCount && idx == -1; i++) 
            if(curr.keys[i].compareTo(key) == 0) idx = i;
            
        if(idx != -1) {
            for(int i = idx; i < curr.keyCount; i++) 
                curr.keys[i] = curr.keys[i + 1];

            curr.keyCount--;
            res = curr;
        }

        return res;
    }

    private Page<T> delete(Object key, Page<T> curr) throws IOException {
        Page<T> child = null;

        if(curr.children[0] == -1) {
            child = this.deleteChild(key, curr);
        } else {
            int idx = 0;
            while(idx < curr.keyCount && curr.keys[idx].compareTo(key) < 0) {
                idx++;
            }

            if(curr.keys[idx].compareTo(key) == 0) {
                this.addressToRewrite = curr.address;
                this.indexToRewrite = idx;
                idx++;
            }

            child = this.delete(key, this.readPage(curr.children[idx]));

            if(child.isUnderflow()) {
                Page<T> left = (idx > 0) ? this.readPage(curr.children[idx - 1]) : null;
                Page<T> right = (idx < curr.keyCount) ? this.readPage(curr.children[idx + 1]) : null;

                if(left != null && left.canBorrow()) {
                    for(int j = child.keyCount; j > 0; j--) {
                        child.keys[j] = child.keys[j - 1];
                        child.children[j + 1] = child.children[j];
                    }

                    child.children[1] = child.children[0];

                    if(child.children[0] == -1) {
                        child.keys[0] = left.keys[left.keyCount - 1].clone();
                        child.keyCount++;
    
                        try {
                            left.keys[left.keyCount - 1] = constructor.newInstance();
                        } catch(Exception e) {
                            System.out.println("Can not make a new instance of " + constructor.getClass().getName() + ".");
                            e.printStackTrace();
                        }

                        left.keyCount--;
    
                        curr.keys[idx - 1] = child.keys[0];
                    } else {    
                        child.keys[0] = curr.keys[idx - 1].clone();
                        child.children[0] = left.children[left.keyCount];
                        child.keyCount++;
    
                        curr.keys[idx - 1] = left.keys[left.keyCount - 1].clone();

                        try {
                            left.keys[left.keyCount - 1] = constructor.newInstance();
                        } catch(Exception e) {
                            System.out.println("Can not make a new instance of " + constructor.getClass().getName() + ".");
                            e.printStackTrace();
                        }

                        left.children[left.keyCount] = -1;
                        left.keyCount--;
                    }
                    
                    this.writePage(child, child.address);
                    this.writePage(left, left.address);
                    this.writePage(curr, curr.address);
                } else if(right != null && right.canBorrow()) {
                    if(child.children[0] == -1) {
                        child.keys[child.keyCount] = right.keys[0].clone();
                        child.keyCount++;

                        for(int j = 0; j < right.keyCount; j++) {
                            right.keys[j] = right.keys[j + 1];
                        }
                        right.keyCount--;

                        curr.keys[idx] = right.keys[0];
                    } else {
                        child.keys[child.keyCount] = curr.keys[idx].clone();
                        child.children[child.keyCount + 1] = right.children[0];
                        child.keyCount++;

                        curr.keys[idx] = right.keys[0].clone();

                        for(int j = 0; j < right.keyCount; j++) {
                            right.keys[j] = right.keys[j + 1];
                            right.children[j] = right.children[j + 1];
                        }

                        right.children[right.keyCount] = right.children[right.keyCount + 1];
                        right.keyCount--;
                    }
                    
                    this.writePage(child, child.address);
                    this.writePage(right, right.address);
                    this.writePage(curr, curr.address);
                } else if(left != null) {
                    if(child.children[0] == -1) {
                        for(int j = 0; j < child.keyCount; j++) {
                            left.keys[left.keyCount] = child.keys[j].clone();
                            left.keyCount++;
                        }
                        
                        left.next = child.next;
                    } else {
                        left.keys[left.keyCount] = curr.keys[idx - 1].clone();
                        left.keyCount++;

                        for(int j = 0; j < child.keyCount; j++) {
                            left.keys[left.keyCount] = child.keys[j].clone();
                            left.children[left.keyCount] = child.children[j];
                            left.keyCount++;
                        }

                        left.children[left.keyCount] = child.children[child.keyCount];
                    }

                    for(int j = idx - 1; j < curr.keyCount; j++) {
                        curr.keys[j] = curr.keys[j + 1];
                        curr.children[j + 1] = curr.children[j + 2];
                    }

                    curr.keyCount--;

                    this.writePage(left, left.address);
                    this.writePage(curr, curr.address);

                    child = curr;
                } else if(right != null) {
                    if(child.children[0] == -1) {
                        for(int j = 0; j < right.keyCount; j++) {
                            child.keys[child.keyCount] = right.keys[j].clone();
                            child.keyCount++;
                        }
                        
                        child.next = right.next;
                    } else {
                        child.keys[child.keyCount] = curr.keys[idx].clone();
                        child.keyCount++;

                        for(int j = 0; j < right.keyCount; j++) {
                            child.keys[child.keyCount] = right.keys[j].clone();
                            child.children[child.keyCount] = right.children[j];
                            child.keyCount++;
                        }

                        child.children[child.keyCount] = right.children[right.keyCount];
                    }

                    for(int j = idx; j < curr.keyCount; j++) {
                        curr.keys[j] = curr.keys[j + 1];
                        curr.children[j + 1] = curr.children[j + 2];
                    }

                    curr.keyCount--;

                    this.writePage(child, child.address);
                    this.writePage(curr, curr.address);

                    child = curr;
                } else {
                    throw new RuntimeException("This should never happen");
                }
            } else {
                this.writePage(child, child.address);
            }
        }

        return child;
    }

    private Page<T> insert(T key, Page<T> curr) throws IOException {
        int idx = 0;
        Page<T> newPage = null;
        if(curr.children[0] == -1) {
            while(idx < curr.keyCount && curr.keys[idx].compareTo(key) < 0) {
                idx++;
            }
    
            if(curr.keys[idx].compareTo(key) == 0)
                return null;

            for(int i = curr.keyCount - 1; i >= idx; i--) 
                curr.keys[i + 1] = curr.keys[i];

            curr.keys[idx] = key;
            curr.keyCount++;

            if(curr.keyCount > order - 1) {
                newPage = split(curr);
            }

            if(newPage != null) this.writePage(newPage, newPage.address);
            else this.writePage(curr, curr.address);
        } else {
            while(idx < curr.keyCount && curr.keys[idx].compareTo(key) <= 0) {
                idx++;
            }

            Page<T> child = this.readPage(curr.children[idx]);
            newPage = this.insert(key, child);

            if(newPage != null) {
                for(int i = curr.keyCount - 1; i >= idx; i--) {
                    curr.keys[i + 1] = curr.keys[i];
                    curr.children[i + 2] = curr.children[i + 1];
                }

                curr.keys[idx] = newPage.keys[0].clone();
                curr.children[idx + 1] = newPage.address;
                curr.keyCount++;

                if(child.children[0] != -1) {
                    for(int i = 0; i < newPage.keyCount; i++) {
                        newPage.keys[i] = newPage.keys[i + 1];
                    }

                    newPage.keyCount--;
                }

                this.writePage(newPage, newPage.address);
                newPage = (curr.keyCount > this.order - 1) ? split(curr) : null;
                if(newPage == null) this.writePage(curr, curr.address);
            } else {
                this.writePage(curr, curr.address);    
            }
        }

        return newPage;
    }

    private Page<T> split(Page<T> curr) throws IOException {
        int mid = curr.keyCount / 2;

        Page<T> page = new Page<T>(order, this.constructor);
        page.address = this.length();

        if(curr.children[0] == -1) {
            page.next = curr.next;
            curr.next = page.address;
        }

        for (int i = mid; i < curr.keyCount; i++) {
            page.keys[i - mid] = curr.keys[i];
            page.children[i - mid] = curr.children[i + 1];

            try {
                curr.keys[i] = constructor.newInstance();
            } catch(Exception e) {
                System.out.println("Can not make a new instance of " + constructor.getClass().getName() + ".");
                e.printStackTrace();
            }

            curr.children[i + 1]  = -1;
        }

        page.keyCount = curr.keyCount - mid;
        curr.keyCount = mid;

        this.writePage(curr, curr.address);
        return page;
    }

    private T minKey(Page<T> curr) throws IOException {
        if(curr == null) {
            try {
                return constructor.newInstance();
            } catch(Exception e) {
                System.out.println("Can not make a new instance of " + constructor.getClass().getName() + ".");
                e.printStackTrace();
            }
        }

        if(curr.children[0] == -1) {
            return curr.keys[0];
        }

        return this.minKey(this.readPage(curr.children[0]));
    }

    private void toJsonFile(StringBuffer sb, Page<T> curr) throws IOException {
        if(curr != null) {
            toJsonFile(sb, this.readPage(curr.children[0]));
            sb.append(curr.toString() + ",\n");
            
            for(int i = 1; i < this.order; i++)
                toJsonFile(sb, this.readPage(curr.children[i]));
        }
    }

    // Read and Write

    private Page<T> readPage(long address) throws IOException {
        if(address == -1) return null;

        this.file = new RandomAccessFile(this.path, "r");
        this.file.seek(address);

        byte[] buffer = new byte[this.PAGE_BYTES];
        this.file.read(buffer);
        Page<T> page = new Page<T>(buffer, this.constructor);
        page.address = address;

        this.file.close();
        return page;
    }

    private void writePage(Page<T> page, long address) throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.seek(address);

        byte[] buffer = page.toByteArray();
        this.file.write(buffer);

        this.file.close();
    }

    private void updateRoot(long address) throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.seek(0);
        this.file.writeLong(address);
        this.file.close();
        this.root = address;
    }
}
