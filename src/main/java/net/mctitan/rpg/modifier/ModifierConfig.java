package net.mctitan.rpg.modifier;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.tables.tagged.TaggedTable;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class ModifierConfig implements Logger {
    private String id;
    private ConfigurationSection section;

    public ModifierConfig(String id) {
        this(id, Aurivale.instance().getConfig("modifiers").getConfigurationSection(id));
    }

    public ModifierConfig(String id, ConfigurationSection section) {
        this.id = id;
        this.section = section;
    }

    public static ModifierConfig valueOf(String name) { return Modifiers.instance().config(name); }

    public static ModifierConfig modifierconfig(ConfigurationSection section) {
        String modifierclazz = String.format("net.mctitan.rpg.modifier.configs.%s",section.getString("type"));
        try {
            Class<? extends ModifierConfig> clazz = Class.forName(modifierclazz).asSubclass(ModifierConfig.class);
            Constructor<? extends ModifierConfig> constructor = clazz.getConstructor(String.class, ConfigurationSection.class);
            return constructor.newInstance(section.getName(), section);
        } catch(Exception e) {
            Logger.LOG(Level.WARNING, String.format("Error loading %s %s: %s", section.getName(), e.getClass().getSimpleName(), e.getMessage()));
        }

        return null;
    }

    public String id() { return id; }
    protected ConfigurationSection section() { return section; }

    public String name() { return section().getString("name"); }
    public ModifierSlot slot() { return ModifierSlot.valueOf(section().getString("slot")); }
    public boolean base() { return section().getBoolean("base"); }
    public boolean monster() { return section().getBoolean("monster"); }

    public Set<Integer> ranks() { return section().getConfigurationSection("ranks")
            .getKeys(false)
            .stream()
            .map(Integer::parseInt)
            .collect(Collectors.toCollection(TreeSet::new));
    }

    public List<String> tags() {
        if(!section().contains("tags")) {
            return List.of();
        }

        List<String> tags = section().getStringList("tags");
        tags.add(slot().keyname());
        return tags;
    }

    public int weight(int rank) { return section().getInt(String.format("ranks.%d.weight", rank)); }

    public TaggedTable<ModifierTemplate> table() {
        // returned table
        TaggedTable<ModifierTemplate> table = new TaggedTable<>();

        try {
            for (int rank : ranks()) {
                int weight = weight(rank);
                ModifierTemplate template = template(rank);
                List<String> tags = new LinkedList<>(tags());
                table.insert(template, weight, id(), tags);
            }
        } catch(Exception e) {
            log(Level.SEVERE, String.format("Cannot load table for modifier=%s", id()), e);
        }

        return table;
    }

    public abstract Class<? extends Modifier> modifierclass();
    public abstract ModifierTemplate template(int rank);
}
