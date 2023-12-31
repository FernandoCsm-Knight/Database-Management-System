package crud;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import components.interfaces.Register;
import crud.base.StructureValidation;
import crud.core.DataBase;
import crud.core.Trash;
import crud.core.pattern_matching.BoyerMoore;
import crud.core.pattern_matching.KMP;
import crud.core.pattern_matching.Matcher;
import crud.core.pattern_matching.RabinKarp;
import crud.core.types.IndexType;
import crud.core.types.PatternMatchingType;
import crud.core.types.Response;
import crud.core.types.SortType;
import crud.indexes.hash.ExtensibleHash;
import crud.indexes.query.InvertedIndex;
import crud.indexes.trees.BPlusTree;
import crud.indexes.types.NNode;
import crud.indexes.types.SNode;
import crud.sorts.SortedFile;
import crud.sorts.SortedFileFirst;
import crud.sorts.SortedFileHeap;
import crud.sorts.SortedFileSecond;
import err.DecompressException;
import layout.components.MenuCompressionResponse;
import logic.SystemSpecification;
import utils.csv.CSVManager;
import utils.helpers.WatchTime;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class CRUD<T extends Register<T>> implements SystemSpecification {
    
    // Attributes

    private static int FILE_COUNT = 0; // Number of files created.
    private final String filePath; // File name for the CRUD system.
    private final String fileName; // File name for the CRUD system.
    private final DataBase<T> archive; // Database for storing records.
    private final Trash<T> trash; // Trash for deleted records.
    private final Constructor<T> constructor; // Constructor for creating record instances.

    private BPlusTree<NNode> tree = null; // B+ Tree index for records.
    private ExtensibleHash<NNode> hash = null; // Extensible Hash index for records.
    private InvertedIndex invertedIndex = null; // Inverted Index for records.
    private IndexType[] indexTypes = null; // Array of index types used in the CRUD system.

    private int numberOfBranches = -1; // Number of branches for sorting records.
    private int numberOfRegistersPerBlock = -1; // Number of registers per block for sorting records.

    // Constructors 
    
    /**
     * Initializes a new CRUD system with the specified path, constructor, and index types.
     *
     * @param path        The path to the CRUD system.
     * @param constructor The constructor for creating record instances.
     * @param indexTypes  The index types to be used in the CRUD system.
     * @throws Exception if an error occurs during initialization.
     */
    public CRUD(String path, Constructor<T> constructor, IndexType... indexTypes) throws Exception {
        this.fileName = path.substring(0, path.lastIndexOf("."));
        this.filePath = PROJECT_CRUD_PATH + path;
        this.archive = new DataBase<T>("Show DataBase", this.filePath, constructor);
        this.trash = new Trash<T>("Trash", this.filePath + ".trash", constructor);
        this.constructor = constructor;
        this.indexTypes = indexTypes;

        StructureValidation.verifyDirectoryStructure();
        
        if(indexTypes.length > 0) {
            ArrayList<IndexType> indexTypesList = new ArrayList<>();
            for(IndexType indexType : indexTypes) 
                indexTypesList.add(indexType);
            
            if(indexTypesList.contains(IndexType.BPlusTree) && indexTypesList.contains(IndexType.Hash)) {
                int indexBPT = indexTypesList.indexOf(IndexType.BPlusTree);
                int indexHash = indexTypesList.indexOf(IndexType.Hash);

                indexTypesList.remove(indexBPT < indexHash ? indexHash : indexBPT);
            }
                
            for(IndexType indexType : indexTypesList) {
                if(indexType.equals(IndexType.BPlusTree) && this.tree == null) {
                    this.tree = new BPlusTree<NNode>(8, this.fileName + "_BplusTree.db", NNode.class.getConstructor());
                } else if(indexType.equals(IndexType.Hash) && this.hash == null) {
                    this.hash = new ExtensibleHash<NNode>(this.fileName + "_Hash.db", NNode.class.getConstructor());
                } else if(indexType.equals(IndexType.InvertedIndex) && this.invertedIndex == null) {
                    this.invertedIndex = new InvertedIndex(this.fileName + "_InvertedIndex.db");
                } else if(!indexType.equals(IndexType.BPlusTree) && !indexType.equals(IndexType.Hash) && !indexType.equals(IndexType.InvertedIndex)) {
                    throw new IllegalArgumentException("The argument \"" + indexType + "\" is not a valid index type.");
                }
            }
        }
    }

    // Public Methods

    /**
     * Retrieves the name of the CRUD system's file path.
     * @return The name of the CRUD system's file path.
     */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * Retrieves the length of the CRUD system's file.
     * @return The length of the CRUD system's file.
     * @throws IOException if an I/O error occurs.
     */
    public long length() throws IOException {
        return this.archive.length();
    }

    /**
     * Populates the CRUD system with records from a CSV file.
     *
     * @param CSVpath The path to the CSV file.
     * @throws IOException if an I/O error occurs.
     */
    public void populateAll(String CSVpath) throws Exception {
        CSVManager manager = new CSVManager(CSV_FILES_DIRECTORY + CSVpath);
        this.clear();

        try {
            String[] arr = manager.readNext();
            while(arr != null) {
                T obj = this.constructor.newInstance();
                obj.from(arr);
                this.create(obj);
                arr = manager.readNext();
            }
        } catch(Exception e) {
            System.err.println("The file " + this.filePath + " has a register that is not from the given type at line .");
            e.printStackTrace();
        }

        this.archive.reset();
        manager.close();
    }

    /**
     * Rebuilds the specified indexes for the CRUD system.
     *
     * @param indexTypes The index types to be rebuilt.
     * @throws Exception if an error occurs during index rebuilding.
     */
    public void rebuildIndex(IndexType... indexTypes) throws Exception {
        for(IndexType index : indexTypes) {
            if(index.equals(IndexType.BPlusTree) && this.tree != null) {
                this.tree.clear();

                this.archive.reset();
                while(!this.archive.isEOF()) {
                    Response<T> response = this.archive.readObj();
                    T obj = response.body;
                    this.tree.insert(obj.get(obj.getBPlusTreeAttribute()), response.currentAddress);
                }
            } else if(index.equals(IndexType.Hash) && this.hash != null) {
                this.hash.clear();

                this.archive.reset();
                while(!this.archive.isEOF()) {
                    Response<T> response = this.archive.readObj();
                    T obj = response.body;
                    this.hash.insert(obj.get(obj.getExtensibleHashAttribute()), response.currentAddress);
                }
            } else if(index.equals(IndexType.InvertedIndex) && this.invertedIndex != null) {
                this.invertedIndex.clear();

                this.archive.reset();
                while(!this.archive.isEOF()) {
                    Response<T> response = this.archive.readObj();
                    T obj = response.body;
                    this.invertedIndex.insert((String)obj.get(obj.getInvertedIndexAttributes()[0]), response.currentAddress);
                }
            }
        }
    }

    /**
     * Converts the CRUD system's data to JSON files.
     *
     * @throws IOException if an I/O error occurs during JSON file generation.
     */
    public void toJsonFile() throws IOException {
        String path = JSON_FILES_DIRECTORY + this.fileName + (++FILE_COUNT) + ".json";
        this.archive.toJsonFile(path);

        path = JSON_TRASH_FILES_DIRECTORY + this.fileName + FILE_COUNT + ".trash.json";
        this.trash.toJsonFile(path);

        this.indexesToJsonFile();
    }

    /**
     * Converts the CRUD system's data to a JSON file with the specified index.
     *
     * @param fileIndex The index for the JSON file.
     * @throws IOException if an I/O error occurs during JSON file generation.
     */
    public void toJsonFile(int fileIndex) throws IOException {
        String path = JSON_FILES_DIRECTORY + this.fileName + fileIndex + ".json";
        this.archive.toJsonFile(path);

        path = JSON_TRASH_FILES_DIRECTORY + this.fileName + fileIndex + ".trash.json";
        this.trash.toJsonFile(path);

        this.indexesToJsonFile();
    }

    /**
     * Checks if a record with the specified key and value exists in the CRUD system.
     *
     * @param key   The key to search for.
     * @param value The value to search for.
     * @return A response containing information about the search result.
     * @throws Exception if an error occurs during the search.
     */
    public Response<T> contains(String key, Object value) throws Exception {
        T inst = this.constructor.newInstance();
        Response<T> response = new Response<T>();

        if(this.tree != null && inst.getBPlusTreeAttribute().equals(key)) {
            NNode node = this.tree.search(value);

            if(node != null) {
                response.success = true;
                response.message = "The key \"" + value + "\" was found in the hash index.";
                response.currentAddress = (long)node.getValue();
            } else {
                response.message = "The key \"" + value + "\" was not found in the hash index.";
            }
        } else if(this.hash != null && inst.getExtensibleHashAttribute().equals(key)) {
            NNode node = this.hash.search(value);

            if(node != null) {
                response.success = true;
                response.message = "The key \"" + value + "\" was found in the hash index.";
                response.currentAddress = (long)node.getValue();
            } else {
                response.message = "The key \"" + value + "\" was not found in the hash index.";
            }
        } else {
            long address = this.archive.search(key, value);

            if(address != -1) {
                response.success = true;
                response.message = "The key \"" + value + "\" was found in the archive.";
                response.currentAddress = address;
            } else {
                response.message = "The key \"" + value + "\" was not found in the archive.";
            }
        }

        return response;
    }

    /**
     * Creates a new record in the CRUD system.
     *
     * @param obj The record to create.
     * @return `true` if the record creation is successful, `false` otherwise.
     * @throws Exception if an error occurs during record creation.
     */
    public boolean create(T obj) throws Exception {
        long address = this.archive.create(obj).currentAddress;

        if(this.tree != null) 
            this.tree.insert(obj.get(obj.getBPlusTreeAttribute()), address);
        if(this.hash != null)
            this.hash.insert(obj.get(obj.getExtensibleHashAttribute()), address);
        if(this.invertedIndex != null) 
            this.invertedIndex.insert((String)obj.get(obj.getInvertedIndexAttributes()[0]), address);

        return true;
    }

    /**
     * Reads a single record from the CRUD system.
     *
     * @return The record read from the system.
     * @throws IOException if an I/O error occurs during record reading.
     */
    public T read() throws IOException {
        return this.archive.readObj().body;
    }

    /**
     * Reads a range of records from the CRUD system.
     *
     * @param startId The starting ID of the range.
     * @param lastId  The ending ID of the range.
     * @return A list of records within the specified range.
     * @throws Exception if an error occurs during record reading.
     */
    public List<T> read(int startId, int lastId) throws Exception {
        List<T> list = new ArrayList<>();

        int range = lastId - startId + 1;
        for(int i = 0; i < range; i++) 
            list.add(this.read("id", startId + i));

        return list;
    }

    /**
     * Reads a record with the specified key and value from the CRUD system.
     *
     * @param key   The key to search for.
     * @param value The value to search for.
     * @return The record matching the key and value, or `null` if not found.
     * @throws Exception if an error occurs during record reading.
     */
    public T read(String key, Object value) throws Exception {
        T inst = this.constructor.newInstance();

        if(this.tree != null && inst.getBPlusTreeAttribute().equals(key)) {
            NNode node = this.tree.search(value);
            return node != null ? this.archive.readObj((long)node.getValue()) : null;
        } else if(this.hash != null && inst.getExtensibleHashAttribute().equals(key)) {
            NNode node = this.hash.search(value);
            return (node != null) ? this.archive.readObj((long)node.getValue()) : null;
        }

        return this.archive.readObj(key, value).body;
    }

    /**
     * Reads all records with the specified key and value from the CRUD system.
     *
     * @param key   The key to search for.
     * @param value The value to search for.
     * @return An array of records matching the key and value.
     * @throws Exception if an error occurs during record reading.
     */
    @SuppressWarnings("unchecked")
    public T[] readAllObj(String key, Object value) throws Exception {
        if(this.invertedIndex != null && this.constructor.newInstance().getInvertedIndexAttributes()[0].equals(key)) {
            SNode[] nodes = this.invertedIndex.search(((String)value));
            T[] res = (T[])new Register[nodes.length];

            for(int i = 0; i < nodes.length; i++) {
                long address = (long)nodes[i].getValue();
                if(address != -1L) res[i] = this.archive.readObj(address);
            }

            return res;
        } 

        return this.archive.readAllObj(key, value);
    }

    /**
     * Updates a record with the given ID in the CRUD system.
     *
     * @param id  The ID of the record to update.
     * @param obj The updated record.
     * @return `true` if the update is successful, `false` otherwise.
     * @throws Exception if an error occurs during record updating.
     */
    public boolean update(int id, T obj) throws Exception {
        Response<T> response = this.archive.update(id, obj);
        
        if(response.success) {
            if(this.tree != null)
                this.tree.update(id, response.currentAddress);
            else if(this.hash != null)
                this.hash.update(id, response.currentAddress);
            else if(this.invertedIndex != null)
                this.invertedIndex.update((String)obj.get(obj.getInvertedIndexAttributes()[0]), response.oldAddress, response.currentAddress);
        }
        
        return response.success;
    }

    /**
     * Updates a record with the given ID in the CRUD system.
     *
     * @param id    The ID of the record to update.
     * @param key   The key to update.
     * @param value The new value for the key.
     * @return `true` if the update is successful, `false` otherwise.
     * @throws Exception if an error occurs during record updating.
     */
    public boolean update(int id, String key, Object value) throws Exception {
        Response<T> response = this.archive.update(id, key, value);

        if(response.success) {
            if(this.tree != null)
                this.tree.update(id, response.currentAddress);
            else if(this.hash != null)
                this.hash.update(id, response.currentAddress);
            else if(this.invertedIndex != null && this.constructor.newInstance().getInvertedIndexAttributes()[0].equals(key))
                this.invertedIndex.update((String)value, response.oldAddress, response.currentAddress);
        }

        return response.success;
    }

    /**
     * Deletes a record with the given ID from the CRUD system.
     *
     * @param id The ID of the record to delete.
     * @return `true` if the delete is successful, `false` otherwise.
     * @throws Exception if an error occurs during record deletion.
     */
    public boolean delete(int id) throws Exception {
        boolean value = true;
        this.trash.create(this.read("id", id));

        if(this.tree != null) 
            value &= this.tree.delete(id);
        else if(this.hash != null)
            value &= this.hash.delete(id);
        else if(this.invertedIndex != null) {
            Response<T> response = this.archive.readObj("id", id);
            value &= this.invertedIndex.delete((String)response.body.get(response.body.getInvertedIndexAttributes()[0]), response.currentAddress);
        }

        return this.archive.delete(id) && value;
    }

    /**
     * Get the list of backup files names.
     * 
     * @return The list of backup files names.
     * @throws IOException if an I/O error occurs.
     */
    public ArrayList<String> getBackupFilesNames() throws IOException {
        File file = new File(BACKUP_FILES_DIRECTORY);
        ArrayList<String> arr = new ArrayList<>();
        boolean tmp = false;
        if(file.isDirectory()) {
            for(File fi : file.listFiles()) {
                if(fi.isDirectory()) {
                    for(File f : fi.listFiles()) {
                        if(f.getName().contains(this.fileName)) {
                            arr.add(f.getName());
                            tmp = true;
                        }
                    }
                    
                    if(tmp) break;
                }
            }
        }

        return arr;
    }

    /**
     * Compress the CRUD system's data.
     * 
     * @return A response containing information about the compression.
     * @throws Exception if an error occurs during compression.
     */
    public MenuCompressionResponse compress() throws Exception {
        String fileName = new Date().toString().replaceAll("\s", "_") + "_" + this.fileName + ".db";
        MenuCompressionResponse response = new MenuCompressionResponse(fileName);

        WatchTime watch = new WatchTime();
        watch.start();
        this.archive.compressLZW(LZW_FILES_DIRECTORY + fileName);
        response.timeLZW = watch.stop();
        watch.reset();
        watch.start();
        this.archive.compressHuffman(HUFFMAN_FILES_DIRECTORY + fileName);
        response.timeHuffman = watch.stop();
        
        return response;
    }

    /**
     * Decompress the CRUD system's data.
     * 
     * @param filePath The file path to decompress.
     * @return A response containing information about the decompression.
     * @throws Exception if an error occurs during decompression.
     */
    public MenuCompressionResponse decompress(String filePath) throws Exception {
        MenuCompressionResponse response = new MenuCompressionResponse(filePath);
        WatchTime watch = new WatchTime();
        watch.start();
        this.archive.decompressHuffman(HUFFMAN_FILES_DIRECTORY + filePath);
        response.timeHuffman = watch.stop();
        watch.reset();
        watch.start();
        this.archive.decompressLZW(LZW_FILES_DIRECTORY + filePath);
        response.timeLZW = watch.stop();

        return response;
    }

    /**
     * Delete a backup file of the CRUD system if exists.
     * 
     * @param filePath The file path to delete.
     * @throws Exception if an error occurs during backup deletion.
     */
    public void deleteBackup(String filePath) throws Exception {
        if(!filePath.contains("_")) 
            throw new DecompressException("The backup file name is invalid.");

        File file = new File(HUFFMAN_FILES_DIRECTORY + filePath);
        if(file.exists()) file.delete();

        file = new File(LZW_FILES_DIRECTORY + filePath);
        if(file.exists()) file.delete();
    }

    /**
     * A pattern matching search in the CRUD system that returns a list of records that match the given pattern.
     * 
     * @param type The pattern matching type to use.
     * @param pattern The pattern to search for.
     * @param csvPath The CSV file path to search in.
     * @return A list of records that match the given pattern.
     * @throws Exception if an error occurs during pattern matching.
     */
    public ArrayList<T> match(PatternMatchingType type, String pattern, String csvPath) throws Exception {
        pattern = pattern.toLowerCase();
        Matcher matcher;

        if(type == PatternMatchingType.KMP) {
            matcher = new KMP(pattern.toLowerCase());
        } else if(type == PatternMatchingType.BoyerMoore) {
            matcher = new BoyerMoore(pattern.toLowerCase());
        } else if(type == PatternMatchingType.RabinKarp) {
            matcher = new RabinKarp(pattern.toLowerCase());
        } else throw new IllegalArgumentException("The argument \"" + type + "\" is not a valid pattern matching type.");

        CSVManager manager = new CSVManager(CSV_FILES_DIRECTORY + csvPath);
        
        int index = -1;
        String[] columns = manager.readNext();
        ArrayList<T> list = new ArrayList<>();
        while(columns != null) {
            index = -1;
            for(int i = 0; i < columns.length && index == -1; i++)
                index = matcher.search(columns[i].toLowerCase());

            if(index != -1) {
                T inst = this.constructor.newInstance();
                inst.from(columns);
                list.add(inst);
            }

            columns = manager.readNext();
        }

        manager.close();
        return list;
    }

    /**
     * Restores a previously deleted record with the given ID in the CRUD system.
     *
     * @param id The ID of the record to restore.
     * @return `true` if the restore is successful, `false` otherwise.
     * @throws Exception if an error occurs during record restoration.
     */
    public boolean restore(int id) throws Exception {
        if(this.trash.search("id", id) == -1)
            return false;

        Response<T> response = this.archive.restore(id);
        if(!response.success) 
            response = this.archive.create(this.trash.readObj("id", id), true);

        if(this.tree != null) 
            this.tree.insert(response.body.get(response.body.getBPlusTreeAttribute()), response.currentAddress);
        if(this.hash != null)
            this.hash.insert(response.body.get(response.body.getExtensibleHashAttribute()), response.currentAddress);
        if(this.invertedIndex != null) 
            this.invertedIndex.insert((String)response.body.get(response.body.getInvertedIndexAttributes()[0]), response.currentAddress);

        return this.trash.delete(id);
    }

    /**
     * Sorts the records in the CRUD system by the specified key using the given sorting algorithm.
     *
     * @param key       The key to use for sorting.
     * @param algorithm The sorting algorithm to use.
     * @return `true` if the sorting is successful, `false` otherwise.
     * @throws Exception if an error occurs during sorting.
     */
    public boolean orderBy(String key, SortType algorithm) throws Exception {
        SortedFile<T> sorted;

        if(algorithm.equals(SortType.FixedBlocks)) {
            sorted = new SortedFileFirst<T>(this.filePath, T.MAX_REGISTER_SIZE, this.constructor);
        } else if(algorithm.equals(SortType.VariableBlocks)) {
            sorted = new SortedFileSecond<T>(this.filePath, T.MAX_REGISTER_SIZE, this.constructor);
        } else if(algorithm.equals(SortType.Heap)) {
            sorted = new SortedFileHeap<T>(this.filePath, T.MAX_REGISTER_SIZE, this.constructor);
        } else throw new IllegalArgumentException("The argument \"" + algorithm + "\" is not a valid algorithm.");

        if(this.numberOfBranches != -1 && this.numberOfRegistersPerBlock != -1) {
            sorted.setBranches(numberOfBranches);
            sorted.setNumberOfRegistersPerBlock(numberOfRegistersPerBlock);
        }

        sorted.setComparator(constructor.newInstance().getProperties().get(key));
        boolean value = sorted.sort();
        this.restartIndexes();
        return value;
    }

    /**
     * Sorts the records in the CRUD system by the specified key using the default sorting algorithm (Heap Sort).
     *
     * @param key The key to use for sorting.
     * @return `true` if the sorting is successful, `false` otherwise.
     * @throws Exception if an error occurs during sorting.
     */
    public boolean orderBy(String key) throws Exception {
        return this.orderBy(key, SortType.Heap);
    }

    /**
     * Sets the configuration for sorting, including the number of branches and the number of registers per block.
     *
     * @param numberOfBranches         The number of branches to use for sorting.
     * @param numberOfRegistersPerBlock The number of registers per block to use for sorting.
     */
    public void setSortConfig(int numberOfBranches, int numberOfRegistersPerBlock) {
        if(numberOfBranches <= 1) 
            throw new IllegalArgumentException("The argument \"numberOfBranches\" must be greater than 1.");
        
        if(numberOfRegistersPerBlock <= 1) 
            throw new IllegalArgumentException("The argument \"numberOfRegistersPerBlock\" must be greater than 1.");

        this.numberOfBranches = numberOfBranches;
        this.numberOfRegistersPerBlock = numberOfRegistersPerBlock;
    }

    /**
     * Retrieves the list of index types associated with this CRUD instance.
     *
     * @return An ArrayList of IndexType enum values.
     */
    public ArrayList<IndexType> getIndexTypes() {
        ArrayList<IndexType> indexTypes = new ArrayList<>();
        for(IndexType indexType : this.indexTypes) 
            indexTypes.add(indexType);
        
        return indexTypes;
    }

    /**
     * Clears all records from the main archive.
     *
     * @return `true` if the archive is cleared successfully, `false` otherwise.
     * @throws IOException if an I/O error occurs during archive clearing.
     */
    public boolean cleanArchive() throws IOException {
        this.archive.clear();
        return true;
    }

    /**
     * Clears all records from the trash.
     *
     * @return `true` if the trash is cleared successfully, `false` otherwise.
     * @throws IOException if an I/O error occurs during trash clearing.
     */
    public boolean clearTrash() throws IOException {
        this.trash.clear();
        return true;
    }

    /**
     * Checks if the main archive is empty.
     *
     * @return `true` if the archive is empty, `false` otherwise.
     * @throws IOException if an I/O error occurs during checking.
     */
    public boolean isEmpty() throws IOException {
        return this.archive.isEmpty();
    }

    /**
     * Checks if the main archive has reached the end of the file.
     *
     * @return `true` if the end of the file is reached, `false` otherwise.
     * @throws IOException if an I/O error occurs during checking.
     */
    public boolean isEOF() throws IOException {
        return this.archive.isEOF();
    }

    /**
     * Resets the main archive's reading position to the beginning.
     *
     * @throws IOException if an I/O error occurs during resetting.
     */
    public void reset() throws IOException {
        this.archive.reset();
    }

    /**
     * Counts the number of records in the main archive.
     *
     * @return The number of records in the archive.
     * @throws IOException if an I/O error occurs during counting.
     */
    public int count() throws IOException {
        return this.archive.count();
    }

    /**
     * Counts the number of records in the trash.
     *
     * @return The number of records in the trash.
     * @throws IOException if an I/O error occurs during counting.
     */
    public int countTrash() throws IOException {
        return this.trash.count();
    }

    /**
     * Clears all records from the main archive, trash, and associated indexes.
     *
     * @throws IOException if an I/O error occurs during clearing.
     */
    public void clear() throws IOException {
        this.archive.clear();
        this.trash.clear();

        if(this.tree != null) 
            this.tree.clear();

        if(this.hash != null)
            this.hash.clear();

        if(this.invertedIndex != null)
                invertedIndex.clear();
    }

    // Private Methods

    /**
     * Writes indexes data to JSON files if available.
     *
     * @throws IOException if an I/O error occurs during JSON file writing.
     */
    private void indexesToJsonFile() throws IOException {
        if(this.tree != null) 
            this.tree.toJsonFile();

        if(this.hash != null) 
            this.hash.toJsonFile();

        if(this.invertedIndex != null) 
            invertedIndex.toJsonFile();
    }

    /**
     * Restarts and rebuilds the associated indexes if available.
     *
     * @throws Exception if an error occurs during index rebuilding.
     */
    private void restartIndexes() throws Exception {
        if(this.tree != null) {
            this.tree.clear();
            this.rebuildIndex(IndexType.BPlusTree);
        }

        if(this.hash != null) {
            this.hash.clear();
            this.rebuildIndex(IndexType.Hash);
        }

        if(this.invertedIndex != null) {
            invertedIndex.clear();
            this.rebuildIndex(IndexType.InvertedIndex);
        }
    }
}
