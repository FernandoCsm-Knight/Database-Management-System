package crud.indexes.trees;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

import crud.base.StructureValidation;
import crud.indexes.types.interfaces.INode;
import logic.SystemSpecification;

/**
 * <strong> The {@code BPlusTree} class represents a B+ Tree. </strong>
 * 
 * <p>
 * A B+ Tree is a tree data structure that keeps data sorted and allows searches,
 * insertions, and deletions in logarithmic amortized time. It is a self-balancing
 * tree, so the height of the tree is always logarithmic.
 * </p>
 * 
 * <p>
 * The B+ Tree is used to index the data in the database. The B+ Tree is a file
 * that stores the nodes of the tree. The nodes are pages in the file. The pages
 * are blocks of bytes in the disk, so they have a fixed size.
 * </p>
 * 
 * <strong> Logic of the B+ Tree </strong>
 * 
 * <p>
 * The B+ Tree have a root node. The root node is the first node in the file.
 * The root node can be a leaf or an internal node. The root node is the only
 * node that can have less than the minimum number of keys.
 * </p>
 * 
 * <p>
 * The internal nodes have keys and children. The keys are the values that are
 * used to search the nodes. The children are the addresses of the nodes in the
 * file. The internal nodes have one more child than keys.
 * </p>
 * 
 * <p>
 * The leaf nodes have keys and values. The keys are the values that are used to
 * search the nodes. The values are the addresses of the registers in the file.
 * The leaf nodes have the same number of keys and values.
 * </p>
 *
 * <p>
 * The B+ Tree have a dynamic order that is defined when the tree is created.
 * The order is the maximum number of keys that a node can have. The order must
 * be greater than 2.
 * </p>
 * 
 * <p>
 * The B+ Tree uses a minumum number of keys to avoid underflow. The minimum
 * number of keys is the half of the order minus one. The minimum number of
 * keys must be greater than 0. It can be calculated using the formula:
 * </p>
 * 
 * <p>
 * {@code ceil(order / 2.0)) - 1}
 * </p>
 * 
 * 
 * @author Fernando Campos Silva Dal Maria
 * @see crud.indexes.types.interfaces.INode
 * @see crud.indexes.trees.Page
 * 
 * @version 1.0.0
 */

public class BPlusTree<T extends INode<T>> implements SystemSpecification {

    // Attributes

    private static final int MIN_ORDER = 3; // Minimum order of the tree
    
    private int order; // Maximum number of children a node can have
    private String path; // Path of the B+ Tree file
    private long root = -1; // Address of the root node
    private RandomAccessFile file; // File of the B+ Tree
    private Constructor<T> constructor; // Constructor of the node type

    // Delete auxiliar variables

    private long addressToRewrite = -1; // Address of the node that needs to be rewritten
    private int indexToRewrite = -1; // Index of the key that needs to be rewritten

    public final int PAGE_BYTES; // Size of a page in bytes

    // Constructors

    /**
     * Creates a new B+ Tree with the given order and path.
     * 
     * @param order The maximum number of children a node can have.
     * @param path The path of the B+ Tree file.
     * @param constructor The constructor of the node type.
     */
    public BPlusTree(int order, String path, Constructor<T> constructor) {
        if(!path.endsWith(".db"))
            throw new IllegalArgumentException("The B+ Tree name must ends with .db");

        if(order < MIN_ORDER) 
            throw new IllegalArgumentException("Order must be greater than " + MIN_ORDER);

        this.order = order;
        this.constructor = constructor;
        this.path = INDEXES_FILES_DIRECTORY + path;
        this.PAGE_BYTES = new Page<T>(order, constructor).BYTES;

        if(this.length() > 0) {
            try {
                this.file = new RandomAccessFile(this.path, "rw");
                this.file.seek(0);
                this.root = this.file.readLong();
                this.file.close();
            } catch(IOException e) {
                System.out.println("It was not possible to open the tree, please try reset() method");
                e.printStackTrace();
            }
        } else this.init();
    }

    // Initialize Methods

    /**
     * Rewrite and initialize the B+ Tree file.
     * 
     * @throws IOException
     */
    public void reset() throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.setLength(0);
        this.file.close();
        
        this.init();
    }

    // Public Methods

    /**
     * Returns the order of the B+ Tree.
     * 
     * @return The order of the B+ Tree.
     */
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

    /**
     * Returns the node with the given tree.
     * 
     * @param key The key of the node.
     * @return The node with the given key.
     * @throws IOException 
     */
    public T search(Object key) throws IOException {
        return this.search(key, this.readPage(this.root));
    }

    /**
     * Updates the node with the given key setting its value.
     * 
     * @param key The key of the node.
     * @param value The new value of the node.
     * @return True if the node was updated, false otherwise.
     * @throws Exception
     */
    public boolean update(Object key, Object value) throws Exception {
        T node = constructor.newInstance();
        node.setKey(key);
        node.setValue(value);

        return this.update(node, this.readPage(this.root));
    }

    /**
     * Inserts a new node with the given key and value.
     * 
     * @param key The key of the node.
     * @param value The value of the node.
     * @throws Exception 
     */
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

    /**
     * Deletes the node with the given key.
     * 
     * @param key The key of the node.
     * @return True if the node was deleted, false otherwise.
     * @throws IOException
     */
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

    /**
     * Converts the B+ tree to JSON format and stores it in the JSON index directory.
     * 
     * @throws IOException
     */ 
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

    /**
     * Initialize the B+ Tree file.
     * 
     * @return True if the B+ Tree was initialized, false otherwise.
     */
    private boolean init() {
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

    /**
     * Search the node with the given key in the B+ Tree.
     * 
     * @param key The key of the node.
     * @param curr The current page.
     * @return The node with the given key.
     * @throws IOException
     */
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

    /**
     * Search and update the node with the given key in the B+ Tree.
     * 
     * @param key The key of the node.
     * @param curr The current page.
     * @return True if the node was updated, false otherwise.
     * @throws IOException
     */
    private boolean update(T key, Page<T> curr) throws IOException {
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

    /**
     * Delete the node at a child page in the B+ Tree.
     * 
     * @param key The key of the node.
     * @param curr The current page.
     * @return The current page.
     */
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

    /**
     * Delete the node with the given key in the B+ Tree.
     * 
     * @param key The key of the node.
     * @param curr The current page.
     * @return The current page.
     * @throws IOException
     */
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

    /**
     * Insert the node with the given key in the B+ Tree.
     * 
     * @param key The key of the node.
     * @param curr The current page.
     * @return The new page if the current page was split, null otherwise.
     * @throws IOException
     */
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

    /**
     * Split the current page in two.
     * 
     * @param curr The current page.
     * @return The new page.
     * @throws IOException
     */
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

    /**
     * Returns the minimum key for the given page.
     * 
     * @param curr The current page.
     * @return The minimum key for the given page.
     * @throws IOException
     */
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

    /**
     * Converts the B+ Tree to JSON format.
     * 
     * @param sb The string buffer.
     * @param curr The current page.
     * @throws IOException
     */
    private void toJsonFile(StringBuffer sb, Page<T> curr) throws IOException {
        if(curr != null) {
            toJsonFile(sb, this.readPage(curr.children[0]));
            sb.append(curr.toString() + ",\n");
            
            for(int i = 1; i < this.order; i++)
                toJsonFile(sb, this.readPage(curr.children[i]));
        }
    }

    // Read and Write

    /**
     * Read a page from the B+ Tree file.
     * 
     * @param address The address of the page.
     * @return The page.
     * @throws IOException
     */
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

    /**
     * Write a page in the B+ Tree file.
     * 
     * @param page The page.
     * @param address The address of the page.
     * @throws IOException
     */
    private void writePage(Page<T> page, long address) throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.seek(address);

        byte[] buffer = page.toByteArray();
        this.file.write(buffer);

        this.file.close();
    }

    /**
     * Update the root of the B+ Tree.
     * 
     * @param address The address of the root.
     * @throws IOException
     */
    private void updateRoot(long address) throws IOException {
        this.file = new RandomAccessFile(this.path, "rw");
        this.file.seek(0);
        this.file.writeLong(address);
        this.file.close();
        this.root = address;
    }
}
