package crud.sorts;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Comparator;

import components.interfaces.Register;
import crud.base.BinaryArchive;
import utils.datastructs.MinHeap;
import utils.datastructs.HeapNode;

/**
 * The {@code SortedFileHeap} class represents a file that can be sorted by any attribute from type {@code T} register using a heap method.
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 * 
 * @see {@link components.interfaces.Register}
 * @see {@link crud.base.BinaryArchive}
 */
public class SortedFileHeap<T extends Register<T>> extends SortedFile<T> {

    // Constructors

    /**
     * Constructs a new {@code SortedFileHeap} with the given file path, register size and constructor.
     * @param path the file path of the archive.
     * @param registerSize the size of each register in bytes.
     * @param constructor the constructor of the register.
     * @throws IOException if an I/O error occurs.
     */
    public SortedFileHeap(String path, int registerSize, Constructor<T> constructor) throws IOException {
        this(path, registerSize, null, constructor);
    }

    /**
     * Constructs a new {@code SortedFileHeap} with the given file path, register size, comparator and constructor.
     * @param path the file path of the archive.
     * @param registerSize the size of each register in bytes.
     * @param comparator the comparator used to sort the registers.
     * @param constructor the constructor of the register.
     * @throws IOException if an I/O error occurs.
     */
    public SortedFileHeap(String path, int registerSize, Comparator<T> comparator, Constructor<T> constructor) throws IOException {
        super(path, registerSize, comparator, constructor);
    }

    // Private Methods

    /**
     * Distributes the registers from the original file to the temporary files.
     * @return the number of blocks in the temporary files.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    protected int distribute() throws IOException {
        int numberOfBlocks = 0,
            j = 0;

        MinHeap<HeapNode<T>> heap = new MinHeap<>(this.originalNumberOfRegistersPerBlock);

        long length = this.database.length();
        while(this.database.getPosition() < length) {
            T obj = this.database.readObj().body;

            if(heap.size() == this.originalNumberOfRegistersPerBlock) {
                HeapNode<T> tmp;

                if(this.comparator.compare(obj, heap.peek().getItem()) >= 0) {
                    tmp = heap.substitute(new HeapNode<T>(obj, heap.peek().getWeight(), comparator));
                } else {
                    tmp = heap.substitute(new HeapNode<T>(obj, heap.peek().getWeight() + 1, comparator));
                    numberOfBlocks = tmp.getWeight() + 1;
                }

                j = tmp.getWeight() % NUMBER_OF_BRANCHES;
                
                this.originalFiles[j]._writeObj(tmp.getItem());
            } else {
                if(heap.size() == 0) 
                    heap.insert(new HeapNode<T>(obj, 0, comparator));
                else
                    heap.insert(new HeapNode<T>(obj, heap.peek().getWeight(), comparator));
            }
        }

        while(heap.size() != 0) {
            HeapNode<T> tmp = heap.remove();
            j = tmp.getWeight() % NUMBER_OF_BRANCHES;
            numberOfBlocks = tmp.getWeight() + 1;

            this.originalFiles[j]._writeObj(tmp.getItem());
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
}