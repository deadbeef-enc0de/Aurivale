package net.mctitan.rpg.loot;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enums.DropLocation;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LootDrops {
    private static final LootDrops instance = new LootDrops();
    private Map<DropLocation, WorldDrops> worlds = new HashMap<>();

    private LootDrops() {}

    public static LootDrops instance() { return instance; }
    public static void initialize() { instance.load(); }

    private void load() {
        Configuration config = Aurivale.instance().getConfig("loot");

        ConfigurationSection worldssection = config.getConfigurationSection("worlds");
        for(String locationname : worldssection.getKeys(false)) {
            DropLocation location = DropLocation.valueOf(locationname.toUpperCase());
            ConfigurationSection worldsection = worldssection.getConfigurationSection(locationname);
            worlds.put(location, new WorldDrops(worldsection));
        }
    }

    public List<ItemStack> drops(Entity entity, int luck, int items) {
        return drops(entity.bukkitentity().getWorld(), luck, items);
    }

    public List<ItemStack> drops(World bukkitworld, int luck, int items) {
        List<ItemStack> drops = new LinkedList<>();
        DropLocation droplocation = DropLocation.location(bukkitworld);
        WorldDrops world = worlds.get(droplocation);

        for(int i = 0; i < items; i++) {
            drops.add(world.drop(luck));
        }

        return drops;
    }
}
