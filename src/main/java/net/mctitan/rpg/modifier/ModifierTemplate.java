package net.mctitan.rpg.modifier;

import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.Math;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Constructor;
import java.util.Random;
import java.util.logging.Level;

public abstract class ModifierTemplate {
    private int rank;
    private boolean base;
    private boolean monster;
    private boolean filter = false;
    private String id;

    /***** Create/Load/Save *****/
    public ModifierTemplate(int rank, boolean base, boolean monster, String id) {
        this.rank = rank;
        this.base = base;
        this.monster = monster;
        this.id = id;
    }

    public ModifierTemplate(PersistentDataContainer pdc) {
        if(pdc.has(Key.RANK_KEY, PersistentDataType.INTEGER)) {
            this.rank = pdc.get(Key.RANK_KEY, PersistentDataType.INTEGER);
        }

        if(pdc.has(Key.BASE_KEY, PersistentDataType.BOOLEAN)) {
            this.base = pdc.get(Key.BASE_KEY, PersistentDataType.BOOLEAN);
        }

        if(pdc.has(Key.MONSTER_KEY, PersistentDataType.BOOLEAN)) {
            this.monster = pdc.get(Key.MONSTER_KEY, PersistentDataType.BOOLEAN);
        }

        if(pdc.has(Key.ID_KEY, PersistentDataType.STRING)) {
            this.id = pdc.get(Key.ID_KEY, PersistentDataType.STRING);
        }
    }

    public ModifierTemplate(ConfigurationSection section) {
        rank = section.contains("rank") ? section.getInt("rank") : 1;
        base = section.getBoolean("base");
        monster = section.getBoolean("monster");
        filter = section.getBoolean("filter");
        id = section.getName();
    }

    public static ModifierTemplate template(PersistentDataContainer pdc) {
        if(!pdc.has(Key.TEMPLATE_CLASS_KEY, PersistentDataType.STRING)) {
            return null;
        }

        String templateclass = pdc.get(Key.TEMPLATE_CLASS_KEY, PersistentDataType.STRING);
        String classname = String.format("net.mctitan.rpg.modifier.templates.%s", templateclass);
        try {
            Class<? extends ModifierTemplate> clazz = Class.forName(classname).asSubclass(ModifierTemplate.class);
            Constructor<? extends ModifierTemplate> constructor = clazz.getConstructor(PersistentDataContainer.class);
            return constructor.newInstance(pdc);
        } catch(Exception e) {
            Logger.LOG(Level.WARNING, String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()));
        }

        return null;
    }

    public static ModifierTemplate template(ConfigurationSection section) {
        String classname = String.format("net.mctitan.rpg.modifier.templates.%s", section.getString("template"));
        try {
            Class<? extends ModifierTemplate> clazz = Class.forName(classname).asSubclass(ModifierTemplate.class);
            Constructor<? extends ModifierTemplate> constructor = clazz.getConstructor(ConfigurationSection.class);
            return constructor.newInstance(section);
        } catch(Exception e) {
            Logger.LOG(Level.WARNING, String.format("template=%s %s: %s", classname, e.getClass().getSimpleName(), e.getMessage()));
        }

        return null;
    }

    public void save(PersistentDataContainer pdc) {
        pdc.set(Key.RANK_KEY, PersistentDataType.INTEGER, rank());
        pdc.set(Key.BASE_KEY, PersistentDataType.BOOLEAN, base());
        pdc.set(Key.MONSTER_KEY, PersistentDataType.BOOLEAN, monster());
        pdc.set(Key.ID_KEY, PersistentDataType.STRING, id());
    }

    /***** Data Element Access *****/
    public int rank() { return rank; }
    public boolean base() { return base; }
    public boolean monster() { return monster; }
    public boolean filter() { return filter; }
    public String id() { return id; }

    public void base(boolean base) {
        this.base = base;
    }

    public final Modifier modifier() { return modifier(Math.random()); }
    public abstract Modifier modifier(Random random);
    public abstract String string();
}
