package net.mctitan.rpg.loot;

import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.tables.tagged.TaggedTable;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.loot.drops.CraftableGroupDrop;
import net.mctitan.rpg.loot.drops.EnchanterDrop;
import net.mctitan.rpg.loot.drops.UniqueDrop;
import net.mctitan.rpg.loot.drops.WandDrop;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class WorldDrops implements Logger {
    private TaggedTable<Drop> droptable = new TaggedTable<>();

    public WorldDrops(ConfigurationSection section) {
        // load enchanters
        if(section.contains("enchanters")) {
            ConfigurationSection enchanterssection = section.getConfigurationSection("enchanters");

            // get tags for all enchanters
            List<String> tags = enchanterssection.getStringList("tags");

            // go through all enchanter types
            ConfigurationSection typessection = enchanterssection.getConfigurationSection("types");
            for(String enchantername : typessection.getKeys(false)) {
                // get enchanter drop and weight
                EnchanterType enchantertype = EnchanterType.valueOf(enchantername);
                EnchanterDrop enchanterdrop = new EnchanterDrop(enchantertype);
                int weight = typessection.getInt(enchantername);

                // add it to the table with tags
                log(Level.INFO, String.format("Adding %s weight=%d", enchantertype.string(), weight));
                droptable.insert(enchanterdrop, weight, "enchanters", tags);
            }
        }

        // load craftable groups
        if(section.contains("craftables")) {
            ConfigurationSection craftablessection = section.getConfigurationSection("craftables");

            // get tags for all enchanters
            List<String> tags = craftablessection.getStringList("tags");

            // get rarity configuration
            ConfigurationSection raritysection = craftablessection.getConfigurationSection("rarity");

            // get all craftable groups
            for(Object mapobj : craftablessection.getList("types")) {
                Map<String, Object> map = (Map<String, Object>) mapobj;
                MemoryConfiguration groupconfig = new MemoryConfiguration();
                groupconfig.addDefaults(map);

                // create the craftable group drop
                int weight = groupconfig.getInt("weight");
                List<String> craftablenames = groupconfig.getStringList("types");
                CraftableGroupDrop craftablegroup = new CraftableGroupDrop(craftablenames, raritysection);

                // add it to the table
                droptable.insert(craftablegroup, weight, "craftables", tags);
            }
        }

        // load unique drop
        if(section.contains("uniques")) {
            ConfigurationSection uniquessection = section.getConfigurationSection("uniques");

            // get tags for uniques
            List<String> tags = uniquessection.getStringList("tags");

            // add uniques drop type
            int weight = uniquessection.getInt("weight");
            UniqueDrop unique = new UniqueDrop();
            droptable.insert(unique, weight, "uniques", tags);
        }

        // load wand drop
        if(section.contains("wands")) {
            ConfigurationSection wandsection = section.getConfigurationSection("wands");

            // get tags for all wands
            List<String> tags = wandsection.getStringList("tags");

            // get all wand levels
            ConfigurationSection levelssection = wandsection.getConfigurationSection("levels");
            for(String levelstr :  levelssection.getKeys(false)) {
                int level = Integer.parseInt(levelstr);
                int weight = levelssection.getInt(levelstr);
                WandDrop wand = new WandDrop(level);
                droptable.insert(wand, weight, "wands", tags);
            }
        }
    }

    public ItemStack drop(int luck) {
        return droptable.get(luck).stack(luck);
    }
}
