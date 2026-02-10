package net.mctitan.rpg.pack;

import net.mctitan.rpg.data.tables.tagged.TaggedTable;
import net.mctitan.rpg.data.tables.tagged.TaggedTableEntry;
import net.mctitan.rpg.data.tables.tagged.TaggedTableSelector;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;

import java.util.List;

public class PackModifierGroup {
    private String id;
    private int weight;
    private TaggedTable<ModifierTemplate> modifiers =  new TaggedTable<>();

    public PackModifierGroup(String id, int weight) {
        this.id = id;
        this.weight = weight;
    }

    public String id() { return id; }
    public int weight() { return weight; }

    public void add(ModifierConfig modifier) { modifiers.add(modifier.table()); }
    public Modifier get() { return modifiers.get().modifier(); }
    public Modifier get(TaggedTableSelector selector) { return modifiers.subset(selector).get().modifier(); }
    public List<TaggedTableEntry<ModifierTemplate>> modifiers() { return modifiers.taggedentries(); }
}
