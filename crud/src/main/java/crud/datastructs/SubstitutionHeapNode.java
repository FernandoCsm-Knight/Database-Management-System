package crud.datastructs;

import java.util.Comparator;

import components.interfaces.Register;

public class SubstitutionHeapNode<T extends Register<T>> implements Comparable<SubstitutionHeapNode<T>> {

    // Atributes

    private T item;
    private int weight;
    private Comparator<T> comparator;

    // Constructor

    public SubstitutionHeapNode(T item, int weight, Comparator<T> comparator) {
        this.item = item;
        this.weight = weight;
        this.comparator = comparator;
    }


    /**
     * Compares two SubstitutionHeapNodes.
     * @param tSubstitutionHeapNode the other SubstitutionHeapNode
     * @return the difference between the weights of the two nodes if they are not equal. Otherwise, the difference between the items of the two nodes.
     */

    @Override
    public int compareTo(SubstitutionHeapNode<T> tSubstitutionHeapNode) {
        if (tSubstitutionHeapNode.weight != this.weight)
            return this.weight - tSubstitutionHeapNode.weight;
        return comparator.compare(this.item, tSubstitutionHeapNode.item);
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
