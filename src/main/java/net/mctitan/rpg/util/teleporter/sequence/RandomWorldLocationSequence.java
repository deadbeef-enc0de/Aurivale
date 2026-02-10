package net.mctitan.rpg.util.teleporter.sequence;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.teleporter.LocationSequence;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class RandomWorldLocationSequence implements LocationSequence {
    // config info
    private World world;
    private int radius;

    // current location
    private Location current;

    public RandomWorldLocationSequence() {
        // get config info
        ConfigurationSection config = Aurivale.instance().getConfig("spawn");
        world = Bukkit.getWorld(config.getString("world"));
        radius = config.getInt("radius");

        // set the current location
        next();
    }

    public Location current() { return current; }

    public void next() {
        // get over world coords
        double x = Math.nextInt(-radius, radius);
        double z = Math.nextInt(-radius, radius);
        double y = world.getMaxHeight()+128;

        // set current location
        current = new Location(world, x, y, z);
    }
}
