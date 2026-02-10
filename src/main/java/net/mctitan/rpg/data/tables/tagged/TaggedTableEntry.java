package net.mctitan.rpg.data.tables.tagged;

import net.mctitan.rpg.data.tables.weighted.WeightedTableEntry;

import java.util.HashSet;
import java.util.Set;

public class TaggedTableEntry<T> extends WeightedTableEntry<T> {
    private String group;
    private Set<String> tags;

    public TaggedTableEntry(T element, int weight, String group, Set<String> tags) {
        super(element, weight);
        this.group = group;
        this.tags = new HashSet<>(tags);
    }

    public String group() { return group; }
    public Set<String> tags() { return new HashSet<>(tags); }

    public TaggedTableEntry<T> clone() { return new TaggedTableEntry<>(element(), weight(), group, tags); }
    public TaggedTableEntry<T> clone(int weight) { return new TaggedTableEntry<>(element(), weight, group, tags); }
}
