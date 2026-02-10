package net.mctitan.rpg.monster;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.logging.Level;

public class Monsters implements Logger {
    private static Monsters instance = new Monsters();
    private static float craftabledrops = 0;
    private Set<String> notfound = new HashSet<>();
    private Map<String, Monster> monsters = new HashMap<>();

    private Monsters() {}

    public static Monsters instance() { return instance; }
    public static float craftabledrops() { return craftabledrops; }

    public static void initialize() { instance.load(); }

    private void load() {
        log(Level.INFO, "Loading monsters");
        Configuration monsterconfig = Aurivale.instance().getConfig("monsters");
        for(String monsterid : monsterconfig.getKeys(false)) {
            // get monster
            ConfigurationSection monstersection = monsterconfig.getConfigurationSection(monsterid);
            Monster monster = new Monster(monsterid, monstersection);
            monsters.put(monster.name(), monster);
        }

        // set the drop rate for craftables held by entities
        Configuration lootconfig = Aurivale.instance().getConfig("loot");
        craftabledrops = (float)lootconfig.getDouble("monster_craftable_drop_change", 0);
    }

    public Monster get(EntityType type) {
        String monstername = type.name().toLowerCase();
        return get(monstername);
    }

    public Monster get(String monstername) {
        if(!monsters.containsKey(monstername)) {
            if(!notfound.contains(monstername)) {
                log(Level.WARNING, String.format("%s not found in config", monstername));
                notfound.add(monstername);
            }
            return monsters.get("default");
        }

        return monsters.get(monstername);
    }
}
