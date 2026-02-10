package net.mctitan.rpg.dimension;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.dimension.Dimension;
import net.mctitan.rpg.data.dimension.StoryDimension;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.Math;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Dimensions implements Logger {
    private static final Dimensions instance = new Dimensions();

    private final Map<String, Dimension> dimensions = new HashMap<>();
    private final Map<String, StoryDimension> stories = new HashMap<>();
    private long seed;

    private Dimensions() {
        // get default seed for dimensions/worlds that are not specified in their config
        // TODO actually do something here
        seed = Math.nextLong();

        // hand permanent dimensions
        ConfigurationSection dimensionssection = Aurivale.instance().getConfig("dimensions").getConfigurationSection("worlds");
        for(String dimensionname : dimensionssection.getKeys(false)) {
            ConfigurationSection dimensionsection = dimensionssection.getConfigurationSection(dimensionname);
            Dimension dimension = dimension(dimensionsection);
            if(dimension == null) {
                log(Level.SEVERE, String.format("Unable to load dimension \"%s\", configuration issue", dimensionname));
                continue;
            }

            synchronized (dimensions) {
                dimensions.put(dimension.name(), dimension);
            }
            log(Level.INFO, String.format("Loaded dimension \"%s\"", dimensionname));
        }

        // handle temporary stories
    }

    public static Dimensions instance() { return instance; }
    public static void initialize() { instance.load(); }

    private void load() {
        // run the following code 1 tick into server life to allow initialization
        Bukkit.getGlobalRegionScheduler().runDelayed(Aurivale.instance(), scheduledTask -> {
            // check all dimensions to see if they need to be loaded
            List<Dimension> dimensions = new LinkedList<>();
            synchronized (this.dimensions) { dimensions.addAll(this.dimensions.values()); }
            synchronized (this.stories) { dimensions.addAll(this.stories.values()); }
            for(Dimension dimension : dimensions) {
                if(!dimension.exists()) {
                    dimension.create();
                }
                Bukkit.getPluginManager().registerEvents(dimension.handler(), Aurivale.instance());
            }
        }, 1);
    }

    public Dimension dimension(ConfigurationSection section) {
        long seed = section.getLong("seed", this.seed);
        Dimension dimension = new Dimension(section.getName(), seed);

        // get environment
        Environment environment = Environment.valueOf(section.getString("environment"));
        if(environment == null) { return null; }

        // get biome generator
        BiomeGenerator biomes;
        try {
            Class clazz = Class.forName(String.format("net.mctitan.rpg.dimension.biome.%s", section.getString("biomes")));
            Constructor<BiomeGenerator> ctor = clazz.getConstructor();
            biomes = ctor.newInstance();
        } catch(Exception e) {
            log(Level.SEVERE, String.format("Missing or Invalid Biome Generator \"%s\"", section.getString("biomes")), e);
            return null;
        }

        // get terrain generator
        TerrainGenerator terrain;
        try {
            Class clazz = Class.forName(String.format("net.mctitan.rpg.dimension.terrain.%s", section.getString("terrain")));
            Constructor<TerrainGenerator> ctor = clazz.getConstructor();
            terrain = ctor.newInstance();
        } catch(Exception e) {
            log(Level.SEVERE, String.format("Missing or Invalid Terrain Generator \"%s\"", section.getString("terrain")), e);
            return null;
        }

        // get populators
        for(String populatorname : section.getStringList("populators")) {
            try {
                Class clazz = Class.forName(String.format("net.mctitan.rpg.dimension.populator.%s", populatorname));
                Constructor<Populator> ctor = clazz.getConstructor();
                terrain.add(ctor.newInstance());
            } catch(Exception e) {
                log(Level.SEVERE, String.format("Missing or Invalid Populator \"%s\"", populatorname), e);
                return null;
            }
        }

        // get handlers
        Handler handler;
        try {
            Class clazz = Class.forName(String.format("net.mctitan.rpg.dimension.handler.%s", section.getString("handler")));
            Constructor<Handler> ctor = clazz.getConstructor();
            handler = ctor.newInstance();
            handler.permissions(section.getConfigurationSection("permissions"));
        } catch(Exception e) {
            log(Level.SEVERE, String.format("Missing or Invalid Handler \"%s\"", section.getString("handler")), e);
            return null;
        }

        // set data for the dimension
        dimension.environment(environment);
        dimension.biomes(biomes);
        dimension.terrain(terrain);
        dimension.handler(handler);

        return dimension;
    }

    public Dimension dimension(World world) { return dimension(world.getName()); }
    public Dimension dimension(String name) {
        synchronized (dimensions) {
            if(dimensions.containsKey(name)) { return dimensions.get(name); }
        }

        return story(name);
    }

    public StoryDimension story(ItemStack stack) {
        return null;
    }

    public StoryDimension story(World world) { return story(world.getName()); }
    public StoryDimension story(String name) {
        synchronized (stories) {
            if (stories.containsKey(name)) { return stories.get(name); }
        }

        return null;
    }

    public void unload(StoryDimension story) {
    }
}
