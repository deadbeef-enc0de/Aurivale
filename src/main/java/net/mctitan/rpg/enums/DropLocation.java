package net.mctitan.rpg.enums;

import org.bukkit.World;

public enum DropLocation {
    WORLD,
    NETHER,
    END,
    STORY,
    ;

    public static DropLocation location(World world) {
        if(world.getName().equals("world")) {
            return WORLD;
        } else if(world.getName().equals("world_nether")) {
            return NETHER;
        } else if(world.getName().equals("world_the_end")) {
            return END;
        }

        return STORY;
    }
}
