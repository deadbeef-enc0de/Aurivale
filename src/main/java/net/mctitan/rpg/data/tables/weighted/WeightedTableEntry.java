package net.mctitan.rpg.data.tables.weighted;

public class WeightedTableEntry<T> {
    private T element;
    private int weight;

    public WeightedTableEntry(T element, int weight) {
        this.element = element;
        this.weight = weight;
    }

    public WeightedTableEntry<T> clone() { return new WeightedTableEntry<>(element, weight); }
    public WeightedTableEntry<T> clone(int weight) { return new WeightedTableEntry<>(element, weight); }

    public T element() { return element; }
    public int weight() { return weight; }

    public void weight(int weight) { this.weight = weight; }
}
