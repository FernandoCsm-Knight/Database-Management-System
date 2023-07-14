package utils.datastructs;

import java.util.Comparator;

import components.interfaces.Register;

public class HeapNode<T extends Register<T>> implements Comparable<HeapNode<T>> {

    // Atributes

    private T item;
    private int weight;
    private Comparator<T> comparator;

    // Constructor

    public HeapNode(T item, int weight, Comparator<T> comparator) {
        this.item = item;
        this.weight = weight;
        this.comparator = comparator;
    }


    /**
     * Compares two HeapNodes.
     * @param tHeapNode the other HeapNode
     * @return the difference between the weights of the two nodes if they are not equal. Otherwise, the difference between the items of the two nodes.
     */

    @Override
    public int compareTo(HeapNode<T> tHeapNode) {
        if (tHeapNode.weight != this.weight)
            return this.weight - tHeapNode.weight;
        return comparator.compare(this.item, tHeapNode.item);
    }

    // Getters and Setters

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
