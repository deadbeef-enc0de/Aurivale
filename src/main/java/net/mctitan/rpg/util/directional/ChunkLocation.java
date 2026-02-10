package net.mctitan.rpg.util.directional;

import net.mctitan.data.BasicData;
import net.mctitan.data.UUID;
import net.mctitan.rpg.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.Objects;

public class ChunkLocation extends BasicData implements Logger {
    private UUID world;
    private Integer x;
    private Integer z;

    public ChunkLocation() {}
    public ChunkLocation(org.bukkit.Location bukkitloc) { this(new Location(bukkitloc)); }
    public ChunkLocation(Location location) { this(location.world(), convert(location.x()), convert(location.z())); }
    public ChunkLocation(Chunk chunk) { this(chunk.getWorld(), chunk.getX(), chunk.getZ()); }
    public ChunkLocation(ChunkLocation location) { this(location.world(), location.x(), location.z()); }
    public ChunkLocation(World world, int x, int z) {
        this.world = new UUID(world.getUID());
        this.x = x;
        this.z = z;
    }

    public World world() { return Bukkit.getWorld(world.uuid()); }
    public Chunk chunk() { return world().getChunkAt(x, z); }
    public int x() { return this.x; }
    public int z() { return this.z; }

    private static int convert(double d) { return (int)Math.floor(d / 16); }

    public ChunkLocation add(int x, int z) { return new ChunkLocation(world(), x() + x, z() + z); }
    public ChunkLocation subtract(int x, int z) { return add(-x, -z); }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof ChunkLocation)) {
            return false;
        }

        ChunkLocation chunklocation = (ChunkLocation) object;
        return world.equals(chunklocation.world) &&
                x == chunklocation.x() &&
                z == chunklocation.z();
    }

    @Override
    public int hashCode() { return Objects.hash(this.world, this.x, this.z); }

    @Override
    public String toString() { return String.format("%s.chunk[%d,%d]", world().getName(), x, z); }
}
