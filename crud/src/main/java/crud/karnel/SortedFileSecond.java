package crud.karnel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;

import components.interfaces.Register;
import crud.base.BinaryArchive;
import crud.base.StructureValidation;
import err.DatabaseValidationException;

/**
 * The {@code SortedFile} class represents a file that can be sorted by any attribute from type {@code T} register.
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 * 
 * @see {@link components.interfaces.Register}
 * @see {@link crud.base.BinaryArchive}
 */
public class SortedFileSecond<T extends Register<T>> extends BinaryArchive<T> {

    private static final int NUMBER_OF_BRANCHES = 4; // Number of branches for the sort algorithm

    private final DataBase<T> database; // Original data file
    private final int registerSize; // Size of each register in bytes
    private final int originalNumberOfRegistersPerBlock; // Number of registers per block calculated for the first step of the sort algorithm
    
    private int numberOfRegistersPerBlock; // Number of registers per block in the temporary files
    private Comparator<T> comparator = (T obj1, T obj2) -> obj1.getId() - obj2.getId(); // Comparator used to sort the registers
    
    private BinaryArchive<T>[] originalFiles; // Files used to store the registers that will be interpolated
    private BinaryArchive<T>[] tmpFiles; // Temporary files used to store the sorted registers

    // Constructors

    /**
     * Constructs a new {@code SortedFile} with the given file path, register size and constructor.
     * @param path the file path of the archive.
     * @param registerSize the size of each register in bytes.
     * @param constructor the constructor of the register.
     * @throws IOException if an I/O error occurs.
     */
    public SortedFileSecond(String path, int registerSize, Constructor<T> constructor) throws IOException {
        this(path, registerSize, null, constructor);
    }

    /**
     * Constructs a new {@code SortedFile} with the given file path, register size, comparator and constructor.
     * @param path the file path of the archive.
     * @param registerSize the size of each register in bytes.
     * @param comparator the comparator used to sort the registers.
     * @param constructor the constructor of the register.
     * @throws IOException if an I/O error occurs.
     */
    public SortedFileSecond(String path, int registerSize, Comparator<T> comparator, Constructor<T> constructor) throws IOException {
        super(path, constructor);

        if(!path.endsWith(".db")) 
            throw new DatabaseValidationException("The file must have the extension \".db\".");
        
        StructureValidation.createTemporaryDirectory();
        
        this.database = new DataBase<T>(path, constructor);
        if(comparator != null) this.comparator = comparator;

        this.registerSize = registerSize;
        this.originalNumberOfRegistersPerBlock = (int)Math.floor(BLOCK_SIZE/(double)this.registerSize);

        this.numberOfRegistersPerBlock = this.originalNumberOfRegistersPerBlock;
        this.__createArchives();
    }

    // Public Methods

    /**
     * Returns the number of registers per block in the temporary files.
     * @return the number of registers per block in the temporary files.
     * @throws IOException if an I/O error occurs.
     */
    public int getNumberOfReistersPerBlock() {
        return this.originalNumberOfRegistersPerBlock;
    }

    /**
     * Sets the comparator used to sort the registers.
     * @param comparator the comparator that will be used to sort the registers.
     */
    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    /**
     * Sorts the file.
     * @throws IOException if an I/O error occurs.
     */
    public boolean sort() throws IOException {
        int numberOfBlocks = this.distribute();

        while(numberOfBlocks > 1) 
            numberOfBlocks = this.interpolate();

        this.database.copy(this.originalFiles[0]);
        this.__close();
        return true;
    }

    // Private Methods

    /**
     * Checks if all registers from a block were readed.
     * @param values the array of booleans that represents the registers.
     * @return {@code true} if all registers were readed, {@code false} otherwise.
     */
    private Boolean __blockWasReaded(Boolean[] values) {
        Boolean value = true;

        for(int i = 0; value && i < values.length; i++)
            value = values[i];

        return value;
    }

    /**
     * Initialize the {@see originalFiles} and {@see tmpFiles} arrays with a set of {@link crud.BinaryArchive}.
     * @throws IOException if an I/O error occurs.
     */
    @SuppressWarnings("unchecked")
    private void __createArchives() throws IOException {
        this.originalFiles = new BinaryArchive[NUMBER_OF_BRANCHES];
        this.tmpFiles = new BinaryArchive[NUMBER_OF_BRANCHES];

        for(int i = 0; i < this.originalFiles.length; i++) {
            this.originalFiles[i] = new BinaryArchive<T>(TEMPORARY_FILES_PATH + (i + 1) + ".dat", this.constructor);
            this.originalFiles[i].file = new RandomAccessFile(this.originalFiles[i].filePath, "rw");
            
            this.tmpFiles[i] = new BinaryArchive<T>(TEMPORARY_FILES_PATH + (i + 1 + NUMBER_OF_BRANCHES) + ".dat", this.constructor);
            this.tmpFiles[i].file = new RandomAccessFile(this.tmpFiles[i].filePath, "rw");
        }
    }

    /**
     * Changes the file pointers of the {@see originalFiles} and {@see tmpFiles} arrays.
     * @throws IOException if an I/O error occurs.
     */
    @SuppressWarnings("unchecked")
    private void __changeOriginalFiles() throws IOException {
        BinaryArchive<T>[] arr = new BinaryArchive[NUMBER_OF_BRANCHES];
        for(int i = 0; i < this.originalFiles.length; i++) {
            this.originalFiles[i].file.setLength(0);
            arr[i] = this.originalFiles[i];
        }

        for(int i = 0; i < this.originalFiles.length; i++) {
            this.originalFiles[i] = this.tmpFiles[i];
            this.tmpFiles[i] = arr[i];
        }
    }

    /**
     * Checks if there is at least one register in the files from {@see originalFiles} array.
     * @return {@code true} if there is at least one register, {@code false} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    private Boolean __haveRegister() throws IOException {
        Boolean value = false;

        for(int i = 0; !value && i < this.originalFiles.length; i++)
            value = !this.originalFiles[i]._isEOF();

        return value;
    }

    /**
     * Distributes the registers from the original file to the temporary files.
     * @return the number of blocks in the temporary files.
     * @throws IOException if an I/O error occurs.
     */
    @SuppressWarnings("unchecked")
    private int distribute() throws IOException {
        int numberOfBlocks = 0,
            j = 0;

        ArrayList<T> arr = new ArrayList<>();
    
        while(this.database.getPosition() < this.database.length()) {
            arr.add(this.database.readObj());

            if(arr.size() == this.originalNumberOfRegistersPerBlock) {
                arr.sort(this.comparator);
                this.originalFiles[j]._writeObjs(arr.toArray((T[])new Register[arr.size()]));
                
                arr.clear();
                j = ++numberOfBlocks % NUMBER_OF_BRANCHES;
            }
        }

        if(arr.size() != 0) {
            arr.sort(this.comparator);
            this.originalFiles[j]._writeObjs(arr.toArray((T[])new Register[arr.size()]));

            numberOfBlocks++;
        }

        this._resetFilePointers(this.originalFiles);

        return numberOfBlocks;
    }

    /**
     * Interpolates the registers from the original files to the temporary files and changes it`s pointers.
     * @return the number of blocks in the original files.
     * @throws IOException if an I/O error occurs.
     */
    private int interpolate() throws IOException {
        int numberOfBlocks = 0,
            i = 0;

        while(this.__haveRegister()) {
            this.readRegistersAndWriteOrdered(this.tmpFiles[i]);
            i = ++numberOfBlocks % this.tmpFiles.length;
        }

        this.numberOfRegistersPerBlock *= NUMBER_OF_BRANCHES;

        this._resetFilePointers(this.tmpFiles);
        this.__changeOriginalFiles();
        this._resetFilePointers(this.originalFiles);

        return numberOfBlocks;
    }

    /**
     * Reads the registers from the original files and writes them in the temporary files in order.
     * @param arc the temporary file.
     * @throws IOException if an I/O error occurs.
     */
    @SuppressWarnings("unchecked")
    private void readRegistersAndWriteOrdered(BinaryArchive<T> arc) throws IOException {
        Boolean[] restrictions = new Boolean[this.originalFiles.length];
        for(int i = 0; i < restrictions.length; i++)
            restrictions[i] = false;

        T[] prev = (T[])new Register[this.originalFiles.length];

        int positionOfMinObj = -1;
        while(!this.__blockWasReaded(restrictions)) {
            T min = null;

            for(int i = 0; i < this.originalFiles.length; i++) {
                if(!this.originalFiles[i]._isEOF() && !restrictions[i]) {
                    T obj = this.originalFiles[i]._readObj();

                    if(prev[i] == null) prev[i] = obj;

                    if(this.comparator.compare(prev[i], obj) <= 0) {
                        if(min == null) {
                            min = obj;
                            positionOfMinObj = i;
                        } else if(this.comparator.compare(obj, min) < 0) {
                                min = obj;
        
                                this.originalFiles[positionOfMinObj]._returnOneRegister();
        
                                positionOfMinObj = i;
                        } else {
                            this.originalFiles[i]._returnOneRegister();
                        }

                        prev[i] = obj;
                    } else {
                        this.originalFiles[i]._returnOneRegister();
                        restrictions[i] = true;
                    }
                } else restrictions[i] = true;
            }

            if(min != null) arc._writeObj(min);
        }
    }

    /**
     * Closes the {@see originalFiles} and {@see tmpFiles} arrays.
     * @throws IOException if an I/O error occurs.
     */
    private void __close() throws IOException {
        for(int i = 0; i < NUMBER_OF_BRANCHES; i++) {
            this.originalFiles[i].file.close();
            this.tmpFiles[i].file.close();
        }

        File[] list = new File(TEMPORARY_FILES_DIRECTORY).listFiles();
        for(int i = 0; i < list.length; i++)
            list[i].delete();
    }
}
