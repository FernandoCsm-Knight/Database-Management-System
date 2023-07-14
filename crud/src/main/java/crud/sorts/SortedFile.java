package crud.sorts;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.Comparator;

import components.interfaces.Register;
import crud.base.BinaryArchive;
import crud.base.StructureValidation;
import crud.karnel.DataBase;
import err.DatabaseValidationException;
import logic.Logic;


/**
 * The {@code SortedFile} class represents a file that can be sorted by any attribute from type {@code T} register.
 * @author Fernando Campos Silva Dal Maria
 * @version 1.0.0
 * 
 * @see {@link components.interfaces.Register}
 * @see {@link crud.BinaryArchive}
 */
public abstract class SortedFile<T extends Register<T>> extends BinaryArchive<T> {
    protected int NUMBER_OF_BRANCHES = 2; // Number of branches for the sort algorithm

    protected final DataBase<T> database; // Original data file
    protected final int registerSize; // Size of each register in bytes
    protected final int originalNumberOfRegistersPerBlock; // Number of registers per block calculated for the first step of the sort algorithm
    
    protected int numberOfRegistersPerBlock; // Number of registers per block in the temporary files
    protected Comparator<T> comparator = (T obj1, T obj2) -> obj1.getId() - obj2.getId(); // Comparator used to sort the registers
    
    protected BinaryArchive<T>[] originalFiles; // Files used to store the registers that will be interpolated
    protected BinaryArchive<T>[] tmpFiles; // Temporary files used to store the sorted registers

    // Constructors

    /**
     * Constructs a new {@code SortedFile} with the given file path, register size and constructor.
     * @param path the file path of the archive.
     * @param registerSize the size of each register in bytes.
     * @param constructor the constructor of the register.
     * @throws IOException if an I/O error occurs.
     */
    public SortedFile(String path, int registerSize, Constructor<T> constructor) throws IOException {
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
    public SortedFile(String path, int registerSize, Comparator<T> comparator, Constructor<T> constructor) throws IOException {
        super(path, constructor);

        if(!path.endsWith(".db")) 
            throw new DatabaseValidationException("The file must have the extension \".db\".");
        
        StructureValidation.createTemporaryDirectory();
        
        this.database = new DataBase<T>(path, constructor);
        if(comparator != null) this.comparator = comparator;

        this.registerSize = registerSize;
        this.originalNumberOfRegistersPerBlock = Logic.database.blockFactor((double)this.registerSize);

        this.numberOfRegistersPerBlock = this.originalNumberOfRegistersPerBlock;
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
        this.__createArchives();
        int numberOfBlocks = this.distribute();

        while(numberOfBlocks > 1) 
            numberOfBlocks = this.interpolate();

        this.database.copy(this.originalFiles[0]);
        this.__close();
        return true;
    }

    public void setBranches(int branches) {
        if(branches > 1)
            this.NUMBER_OF_BRANCHES = branches;
        else 
            throw new DatabaseValidationException("The number of branches must be greater than 1.");
    }

    public int getBranches() {
        return this.NUMBER_OF_BRANCHES;
    }

    // Protected Methods

    /**
     * Checks if all registers from a block were readed.
     * @param values the array of booleans that represents the registers.
     * @return {@code true} if all registers were readed, {@code false} otherwise.
     */
    protected Boolean __blockWasReaded(Boolean[] values) {
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
    protected void __createArchives() throws IOException {
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
    protected void __changeOriginalFiles() throws IOException {
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
    protected Boolean __haveRegister() throws IOException {
        Boolean value = false;

        for(int i = 0; !value && i < this.originalFiles.length; i++)
            value = !this.originalFiles[i]._isEOF();

        return value;
    }

    /**
     * Closes the {@see originalFiles} and {@see tmpFiles} arrays.
     * @throws IOException if an I/O error occurs.
     */
    protected void __close() throws IOException {
        for(int i = 0; i < NUMBER_OF_BRANCHES; i++) {
            this.originalFiles[i].file.close();
            this.tmpFiles[i].file.close();
        }

        File[] list = new File(TEMPORARY_FILES_DIRECTORY).listFiles();
        for(int i = 0; i < list.length; i++)
            list[i].delete();

        this.database.reset();
    }

    // Abstract Methods

    /**
     * Distributes the registers from the {@see database} into the {@see originalFiles} array.
     * @return the number of blocks in the {@see originalFiles} array.
     * @throws IOException if an I/O error occurs.
     */
    protected abstract int distribute() throws IOException;

    /**
     * Interpolates the registers from the {@see originalFiles} array into the {@see tmpFiles} array.
     * @return the number of blocks in the {@see tmpFiles} array.
     * @throws IOException if an I/O error occurs.
     */
    protected abstract int interpolate() throws IOException;
        
}
