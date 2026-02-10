package net.mctitan.rpg.util.directional;

import net.mctitan.data.BasicData;
import net.mctitan.data.UUID;
import net.mctitan.rpg.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class Location extends BasicData implements Logger {
    private UUID world;
    private double x;
    private double y;
    private double z;

    public Location() {}
    public Location(Block block) { this(block.getLocation()); }
    public Location(org.bukkit.Location l) { this(l.getWorld(), l.getX(), l.getY(), l.getZ()); }
    public Location(Location l) { this(l.world(), l.x, l.y, l.z); }
    public Location(World world, double x, double y, double z) {
        this.world = new UUID(world.getUID());
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location clone() { return new Location(this); }

    public Location blockloc() { return new Location(world(), blockx(), blocky(), blockz()); }
    public Location center() { return new Location(world(), blockx() + 0.5, blocky() + 0.5, blockz() + 0.5); }

    public Vector vector() { return new Vector(this); }
    public World world() { return Bukkit.getWorld(world.uuid()); }
    public Chunk chunk() { return chunklocation().chunk(); }
    public ChunkLocation chunklocation() { return new ChunkLocation(this); }
    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }

    public void world(World world) { this.world = new UUID(world.getUID()); }
    public Location x(double x) { this.x = x; return this; }
    public Location y(double y) { this.y = y; return this; }
    public Location z(double z) { this.z = z; return this; }

    public int blockx() { return (int)Math.floor(x); }
    public int blocky() { return (int)Math.floor(y); }
    public int blockz() { return (int)Math.floor(z); }

    public org.bukkit.Location bukkit() { return new org.bukkit.Location(world(), x, y, z); }
    public org.bukkit.Location bukkit(Vector direction) { return bukkit().setDirection(direction.bukkit()); }
    public boolean isloaded() { return bukkit().getChunk().isLoaded(); }
    public Block block() { return bukkit().getBlock(); }

    public Location add(Vector v) { return add(v.x(), v.y(), v.z()); }
    public Location add(double x, double y, double z) { return new Location(world(), this.x+x, this.y+y, this.z+z); }
    public Location subtract(Vector v) { return subtract(v.x(), v.y(), v.z()); }
    public Location subtract(double x, double y, double z) { return add(-x, -y, -z); }

    public double distance(Location other) {
        return Math.sqrt(
                Math.pow(this.x - other.x, 2) +
                Math.pow(this.y - other.y, 2) +
                Math.pow(this.z - other.z, 2)
        );
    }

    public double fastdistance(Location other) {
        return Math.abs(this.x - other.x) + Math.abs(this.z - other.z) + Math.abs(this.y - other.y) / 2;
    }

    public Location relative(BlockFace direction, double distance) {
        switch(direction) {
            case SOUTH -> { return add(0, 0, distance); }
            case NORTH -> { return subtract(0, 0, distance); }
            case EAST -> { return add(distance, 0, 0); }
            case WEST -> { return subtract(distance, 0, 0); }
            case UP -> { return add(0, distance, 0); }
            case DOWN -> { return subtract(0, distance, 0); }
            case null,default -> log(Level.INFO, String.format("%s direction not handled yet", direction));
        }

        return this;
    }

    public Set<Location> adjacent() {
        return Set.of(
                add(1, 0, 0),
                add(1, 0, 0),
                add(0, 0, 1),
                add(0, 0, 1),
                add(0, 1, 0),
                add(0, 1, 0)
        );
    }

    public Set<Location> diagonaladjacent() {
        Set<Location> ret = new HashSet<>();
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                for (int y = -1; y <= 1; ++y) {
                    if (x == 0 && z == 0 && y == 0) {
                        continue;
                    }
                    ret.add(add(x, y, z));
                }
            }
        }

        return ret;
    }

    public boolean blockequals(Object object) {
        if(!(object instanceof Location)) {
            return false;
        }

        Location location = (Location)object;
        return this.world.equals(location.world) &&
                this.blockx() == location.blockx() &&
                this.blocky() == location.blocky() &&
                this.blockz() == location.blockz();
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof Location)) {
            return false;
        }

        Location location = (Location)object;
        return this.world.equals(location.world) &&
                this.x == location.x &&
                this.y == location.y &&
                this.z == location.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.world, this.x, this.y, this.z);
    }

    @Override
    public String toString() { return String.format("%s[%.01f,%.01f,%.01f]", world().getName(), x, y, z); }
}
