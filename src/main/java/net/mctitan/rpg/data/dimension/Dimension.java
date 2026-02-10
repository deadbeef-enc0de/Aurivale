package net.mctitan.rpg.data.dimension;

import net.mctitan.data.BaseData;
import net.mctitan.rpg.dimension.BiomeGenerator;
import net.mctitan.rpg.dimension.Handler;
import net.mctitan.rpg.dimension.TerrainGenerator;
import net.mctitan.rpg.modifier.Modifier;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;

import java.util.Collection;
import java.util.LinkedList;

public class Dimension extends BaseData {
    private String name;
    private long seed;
    private transient Environment environment;
    private transient BiomeGenerator biomes;
    private transient TerrainGenerator terrain;
    private transient Handler handler;

    private LinkedList<Modifier> playermods = new LinkedList<>();
    private LinkedList<Modifier> monstermods = new LinkedList<>();

    public Dimension() {}

    public Dimension(String name, long seed) {
        this.name = name;
        this.seed = seed;
    }

    public boolean exists() { return Bukkit.getWorld(name) != null; }
    public void create() {
        WorldCreator creator = new WorldCreator(name);
        creator.seed(seed)
                .environment(environment)
                .generator(terrain)
                .biomeProvider(biomes)
                .createWorld();
    }

    public String name() { return name; }
    public long seed() { return seed; }
    public World world() { return Bukkit.getWorld(name()); }
    public Environment environment() { return environment; }
    public BiomeGenerator biomes() { return biomes; }
    public TerrainGenerator terrain() { return terrain; }
    public Handler handler() { return handler; }

    public Collection<Modifier> playermods() { return new LinkedList<>(playermods); }
    public Collection<Modifier> monstermods() { return new LinkedList<>(monstermods); }

    public void environment(Environment environment) { this.environment = environment; }
    public void biomes(BiomeGenerator biomes) { this.biomes = biomes; }
    public void terrain(TerrainGenerator terrain) { this.terrain = terrain; }
    public void handler(Handler handler) { this.handler = handler; this.handler.dimension(this); }

    public void playermod(Modifier modifier) { playermods.add(modifier); }
    public void monstermod(Modifier modifier) { monstermods.add(modifier); }
}
