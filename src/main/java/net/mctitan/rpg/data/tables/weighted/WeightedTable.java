package net.mctitan.rpg.data.tables.weighted;

import net.mctitan.rpg.util.Math;

import java.util.*;

public class WeightedTable<T> {
    private List<WeightedTableEntry<T>> entries = new ArrayList<>();
    private TreeMap<Integer, WeightedTableEntry<T>> mapping = new TreeMap<>();
    private int total;

    public WeightedTable() {}

    private WeightedTable(WeightedTable<T> other) {
        for(WeightedTableEntry<T> entry : other.entries) {
            insert(entry.clone());
        }
    }

    public WeightedTable<T> clone() { return new WeightedTable<>(this); }

    public List<WeightedTableEntry<T>> entries() { return entries; }
    public int size() { return entries.size(); }
    public List<T> elements() {
        List<T> ret = new LinkedList<>();
        for(WeightedTableEntry<T> entry : entries) {
            ret.add(entry.element());
        }

        return ret;
    }
    public int total() { return total; }

    public T get() { return get(0); }
    public T get(int extra) { return get(Math.random(), extra); }
    public T get(Random random) { return get(random, 0); }
    public T get(Random random, int extra) {
        if(total <= 0) {
            return null;
        }

        // if extra is negative make a new temporary table using extra as an offset on the weight
        if(extra < 0) {
            WeightedTable<T> changed = new WeightedTable<>();
            for (WeightedTableEntry<T> entry : entries) {
                int weight = entry.weight() + extra;
                if (weight > 0) {
                    changed.insert(entry.clone(weight));
                }
            }
            return changed.get();
        }

        // get index into the total + extra math
        int rand = random.nextInt(total + extra * entries.size());

        // get normal entry
        if(rand < total) {
            return mapping.floorEntry(rand).getValue().element();
        }

        // get extra entry
        else {
            int index = (rand - total) / extra;
            return entries.get(index).element();
        }
    }

    public void insert(T value, int weight) {
        WeightedTableEntry<T> entry = new WeightedTableEntry<>(value, weight);
        insert(entry);
    }

    public void insert(WeightedTableEntry<T> entry) {
        this.entries.add(entry);
        this.mapping.put(total, entry);
        total += entry.weight();
    }
}
