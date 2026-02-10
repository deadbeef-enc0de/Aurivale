package net.mctitan.rpg.data.tables.tagged;

import net.mctitan.rpg.data.tables.weighted.WeightedTable;
import net.mctitan.rpg.data.tables.weighted.WeightedTableEntry;

import java.util.*;

public class TaggedTable<T> extends WeightedTable<T> {
    private Map<String, Set<TaggedTableEntry<T>>> groups = new TreeMap<>();
    private Map<String, Set<TaggedTableEntry<T>>> tags = new TreeMap<>();

    public TaggedTable() {}

    public List<TaggedTableEntry<T>> taggedentries() {
        List<TaggedTableEntry<T>> ret = new ArrayList<>();
        for(WeightedTableEntry<T> entry : entries()) {
            ret.add((TaggedTableEntry<T>)entry);
        }

        return ret;
    }

    public void add(TaggedTable<T> other) {
        for(TaggedTableEntry<T> entry : other.taggedentries()) {
            insert(entry);
        }
    }

    public void insert(T value, int weight) { throw new RuntimeException("Cannot insert into tagged table without tags"); }

    public void insert(T value, int weight, String group, String ... taglist) {
        Set<String> tags = Set.of(taglist);
        TaggedTableEntry<T> entry = new TaggedTableEntry<>(value, weight, group, tags);
        insert(entry);
    }
    public void insert(T value, int weight, String group, List<String> taglist) {
        Set<String> tags = new LinkedHashSet<>(taglist);
        TaggedTableEntry<T> entry = new TaggedTableEntry<>(value, weight, group, tags);
        insert(entry);
    }
    public void insert(TaggedTableEntry<T> entry) {
        super.insert(entry);

        if(!this.groups.containsKey(entry.group())) { this.groups.put(entry.group(), new LinkedHashSet<>()); }
        this.groups.get(entry.group()).add(entry);

        for(String tag : entry.tags()) {
            if(!this.tags.containsKey(tag)) { this.tags.put(tag, new LinkedHashSet<>()); }
            this.tags.get(tag).add(entry);
        }
    }

    public TaggedTable<T> subset(int whitelist, int blacklist, String ... elements) {
        if((whitelist + blacklist) != elements.length) {
            return null;
        }

        TaggedTableSelector selector = new TaggedTableSelector();
        for(int i = 0; i < whitelist; ++i) {
            selector.whitelist(elements[i]);
        }
        for(int i = 0; i < blacklist; ++i) {
            selector.blacklist(elements[i+whitelist]);
        }

        return subset(selector);
    }

    public TaggedTable<T> subset(TaggedTableSelector selector) {
        Set<TaggedTableEntry<T>> whitelist = new LinkedHashSet<>();
        Set<TaggedTableEntry<T>> blacklist = new LinkedHashSet<>();

        // if there is no whitelist, add everything
        if(selector.whitelist().isEmpty()) {
            whitelist.addAll(taggedentries());
        } else {
            // go through whitelist, add all group or tag objects that match
            for(String check : selector.whitelist()) {
                if(this.groups.containsKey(check)) { whitelist.addAll(this.groups.get(check)); }
                if(this.tags.containsKey(check)) { whitelist.addAll(this.tags.get(check)); }
            }
        }

        // construct the blacklist
        // unlike whitelist empty blacklist means nothing
        for(String check : selector.blacklist()) {
            if(this.groups.containsKey(check)) { blacklist.addAll(this.groups.get(check)); }
            if(this.tags.containsKey(check)) { blacklist.addAll(this.tags.get(check)); }
        }

        // remove black list from the whitelist
        whitelist.removeAll(blacklist);

        // construct a new table
        TaggedTable<T> table = new TaggedTable<>();
        for(TaggedTableEntry<T> entry : whitelist) {
            double likelyhood = 1;
            likelyhood *= selector.likelyhood(entry.group());
            for(String tag : entry.tags()) {
                likelyhood *= selector.likelyhood(tag);
            }
            if(likelyhood != 1) {
                if(likelyhood <= 0) {
                    continue;
                }
                entry = entry.clone();
                entry.weight((int)(entry.weight() * likelyhood));
            }
            table.insert(entry);
        }

        return table;
    }
}
