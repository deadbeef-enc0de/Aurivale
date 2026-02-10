package net.mctitan.rpg.util.teleporter.sequence;

import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.teleporter.LocationSequence;
import org.bukkit.block.BlockFace;

public class AdjacentLocationSequence implements LocationSequence {
    private static final BlockFace[] DIRECTIONS = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    private Location location;
    private Location current;
    private int distance = 0;
    private int index = 0;

    public AdjacentLocationSequence(Location location) {
        this.location = location;
        this.current = location;
    }

    public Location current() { return current; }

    public void next() {
        // increment location to the next one
        if(distance == 0) { distance = 1; }
        else {
            ++index;
            if(index == DIRECTIONS.length) {
                index = 0;
                ++distance;
            }
        }

        // set current to the next location
        current = location.relative(DIRECTIONS[index], distance);
    }
}
