package utils.datastructs;

public class MinHeap<T extends Comparable<T>> {
    private final T[] heap;
    private int size;

    /**
     * Creates a new MinHeap with the given capacity.
     * @param capacity the capacity of the heap
     */
    @SuppressWarnings("unchecked")
    public MinHeap(int capacity) {
        heap = (T[]) new Comparable[capacity];
        size = 0;
    }

    /**
     * Inserts a new element into the heap.
     * @param item the element to insert
     */

    public void insert(T item) {
        if (size == heap.length) {
            throw new IllegalStateException("Heap is full");
        }
        heap[size] = item;
        size++;
        bubbleUp();
    }


    /**
     * Removes the minimum element from the heap.
     * @return the minimum element
     */

    public T remove() {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
        T item = heap[0];
        heap[0] = heap[size - 1];
        size--;
        bubbleDown();
        return item;
    }

    /**
     * Replaces the minimum element in the heap with a new element.
     * @param item the new element
     * @return the minimum element
     */

    public T substitute(T item) {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
        T oldItem = heap[0];
        heap[0] = item;
        bubbleDown();
        return oldItem;
    }

    /**
     * Bubbles down the element at the top of the heap to its correct position.
     * This method is used when removing the minimum element.
     */

    private void bubbleDown() {
        int index = 0;
        while (hasLeftChild(index)) {
            int smallerChildIndex = getLeftChildIndex(index);
            if (hasRightChild(index) && rightChild(index).compareTo(leftChild(index)) < 0) {
                smallerChildIndex = getRightChildIndex(index);
            }
            if (heap[index].compareTo(heap[smallerChildIndex]) < 0) {
                break;
            } else {
                swap(index, smallerChildIndex);
            }
            index = smallerChildIndex;
        }
    }

    /**
     * Bubbles up the element at the bottom of the heap to its correct position.
     * This method is used when inserting a new element.
     */

    private void bubbleUp() {
        int index = size - 1;
        while (hasParent(index) && parent(index).compareTo(heap[index]) > 0) {
            swap(getParentIndex(index), index);
            index = getParentIndex(index);
        }
    }

    /**
     * Returns the minimum element in the heap.
     * @return the minimum element
     */

    public T peek() {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
        return heap[0];
    }

    /**
     * Returns the number of elements in the heap.
     * @return the number of elements
     */

    public int size() {
        return size;
    }


    private boolean hasParent(int i) {
        return getParentIndex(i) >= 0;
    }

    private int getParentIndex(int i) {
        return (i - 1) / 2;
    }

    private T parent(int i) {
        return heap[getParentIndex(i)];
    }

    private boolean hasLeftChild(int i) {
        return getLeftChildIndex(i) < size;
    }

    private int getLeftChildIndex(int i) {
        return 2 * i + 1;
    }

    private T leftChild(int i) {
        return heap[getLeftChildIndex(i)];
    }

    private boolean hasRightChild(int i) {
        return getRightChildIndex(i) < size;
    }

    private int getRightChildIndex(int i) {
        return 2 * i + 2;
    }

    private T rightChild(int i) {
        return heap[getRightChildIndex(i)];
    }

    private void swap(int indexOne, int indexTwo) {
        T temp = heap[indexOne];
        heap[indexOne] = heap[indexTwo];
        heap[indexTwo] = temp;
    }
}
