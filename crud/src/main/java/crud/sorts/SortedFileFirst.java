package crud.sorts;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;

import components.interfaces.Register;
import crud.base.BinaryArchive;

/**
 * The {@code SortedFileFirst} class represents a file that can be sorted by any attribute from type {@code T} register by a simple method.
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 * 
 * @see {@link components.interfaces.Register}
 * @see {@link crud.BinaryArchive}
 */
public class SortedFileFirst<T extends Register<T>> extends SortedFile<T> {

    // Constructors

    /**
     * Constructs a new {@code SortedFileFirst} with the given file path, register size and constructor.
     * @param path the file path of the archive.
     * @param registerSize the size of each register in bytes.
     * @param constructor the constructor of the register.
     * @throws IOException if an I/O error occurs.
     */
    public SortedFileFirst(String path, int registerSize, Constructor<T> constructor) throws IOException {
        this(path, registerSize, null, constructor);
    }

    /**
     * Constructs a new {@code SortedFileFirst} with the given file path, register size, comparator and constructor.
     * @param path the file path of the archive.
     * @param registerSize the size of each register in bytes.
     * @param comparator the comparator used to sort the registers.
     * @param constructor the constructor of the register.
     * @throws IOException if an I/O error occurs.
     */
    public SortedFileFirst(String path, int registerSize, Comparator<T> comparator, Constructor<T> constructor) throws IOException {
        super(path, registerSize, comparator, constructor);
    }

    // Private Methods

    /**
     * Distributes the registers from the original file to the temporary files.
     * @return the number of blocks in the temporary files.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected int distribute() throws IOException {
        int numberOfBlocks = 0,
            j = 0;

        ArrayList<T> arr = new ArrayList<>();
    
        long length = this.database.length();
        while(this.database.getPosition() < length) {
            arr.add(this.database.readObj().body);

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
    @Override
    protected int interpolate() throws IOException {
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
    private void readRegistersAndWriteOrdered(BinaryArchive<T> arc) throws IOException {
        Boolean[] restrictions = new Boolean[this.originalFiles.length];
        for(int i = 0; i < restrictions.length; i++)
            restrictions[i] = false;

        int[] numberOfReadedRegisters = new int[this.originalFiles.length];

        int positionOfMinObj = -1;
        while(!this.__blockWasReaded(restrictions)) {
            T min = null;

            for(int i = 0; i < this.originalFiles.length; i++) {
                if(!this.originalFiles[i]._isEOF() && numberOfReadedRegisters[i] < this.numberOfRegistersPerBlock) {
                    numberOfReadedRegisters[i]++;
                    T obj = this.originalFiles[i]._readObj();

                    if(min == null) {
                        min = obj;
                        positionOfMinObj = i;
                    } else if(this.comparator.compare(obj, min) < 0) {
                            min = obj;
    
                            this.originalFiles[positionOfMinObj]._returnOneRegister();
                            numberOfReadedRegisters[positionOfMinObj]--;
    
                            positionOfMinObj = i;
                    } else {
                        this.originalFiles[i]._returnOneRegister();
                        numberOfReadedRegisters[i]--;
                    }
                } else restrictions[i] = true;
            }

            if(min != null) arc._writeObj(min);
        }
    }
}