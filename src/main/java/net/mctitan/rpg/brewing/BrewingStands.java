package net.mctitan.rpg.brewing;

import net.mctitan.data.UUID;
import net.mctitan.data.saver.WorldSaver;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.brewing.BrewingStand;
import net.mctitan.rpg.util.directional.ChunkLocation;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BrewingStands {
    private static BrewingStands instance = new BrewingStands();

    private Map<ChunkLocation, Map<Location, BrewingStand>> standlocations = new HashMap<>();
    private Map<UUID, BrewingStand> standuuids = new HashMap<>();
    private Map<World, WorldSaver> worlds = new HashMap<>();

    private BrewingStands() {}

    public static BrewingStands instance() { return instance; }
    public static void initialize() { instance.load(); }

    private void load() {
        // remove the old brewing stand recipe
        Iterator<Recipe> recipes = Aurivale.instance().getServer().recipeIterator();
        while(recipes.hasNext()) {
            Recipe recipe = recipes.next();
            if(recipe.getResult().getType() == Material.BREWING_STAND) {
                Keyed keyed = (Keyed)recipe;
                Aurivale.instance().getServer().removeRecipe(keyed.getKey());
            }
        }

        // add replacement brewing stand recipe
        NamespacedKey key = new NamespacedKey(Aurivale.instance(), "brewing_stand");
        ShapedRecipe brewingstand = new ShapedRecipe(key, new org.bukkit.inventory.ItemStack(Material.BREWING_STAND));
        brewingstand.shape(" s ","ccc");
        brewingstand.setIngredient('s', Material.STICK);
        brewingstand.setIngredient('c', new RecipeChoice.MaterialChoice(Material.COBBLESTONE, Material.BLACKSTONE, Material.COBBLED_DEEPSLATE));
        Aurivale.instance().getServer().addRecipe(brewingstand);
    }

    public WorldSaver saver(World world) {
        synchronized (worlds) {
            if (!worlds.containsKey(world)) {
                worlds.put(world, WorldSaver.saver(Aurivale.instance(), world));
            }
            return worlds.get(world);
        }
    }

    public void save(BrewingStand brewingstand) {
        Location location = brewingstand.location();
        WorldSaver saver = saver(location.world());
        synchronized (saver) { saver.save(location.chunklocation().x(), location.chunklocation().z(), brewingstand); }
    }

    public void delete(BrewingStand brewingstand) {
        Location location = brewingstand.location();
        WorldSaver saver = saver(location.world());
        synchronized (saver) { saver.remove(location.chunklocation().x(), location.chunklocation().z(), brewingstand); }
    }

    public void create(Location location) {
        // make sure there is not already a brewing stand, if so remove it
        if(stand(location) != null) {
            remove(location);
        }

        // create the bewing stand and add it
        BrewingStand brewingstand = new BrewingStand(location);
        add(brewingstand);

        // save the brewing stand to disk
        save(brewingstand);
    }

    public void remove(Location location) {
        BrewingStand brewingstand = stand(location);
        if(brewingstand == null) {
            return;
        }

        // remove from the internal memory storage
        synchronized (standlocations) {
            if (!standlocations.containsKey(location.chunklocation())) {
                return;
            }
            Map<Location, BrewingStand> chunkstands = standlocations.get(location.chunklocation());

            synchronized (chunkstands) {
                if (!chunkstands.containsKey(location)) {
                    return;
                }
                chunkstands.remove(location);
            }

            if(chunkstands.isEmpty()) {
                standlocations.remove(location.chunklocation());
            }
        }

        // remove stand by uuid
        synchronized (standuuids) { standuuids.remove(brewingstand.uuid); }

        // remove brewing stand from disk
        delete(brewingstand);
    }

    public BrewingStand stand(UUID uuid) { synchronized (standuuids) { return standuuids.get(uuid); } }
    public BrewingStand stand(org.bukkit.Location location) {return stand(new Location(location)); }
    public BrewingStand stand(Location location) {
        synchronized (standlocations) {
            if (!standlocations.containsKey(location.chunklocation())) {
                return null;
            }
            Map<Location, BrewingStand> chunkstands = standlocations.get(location.chunklocation());

            synchronized (chunkstands) {
                if (!chunkstands.containsKey(location)) {
                    return null;
                }
                return chunkstands.get(location);
            }
        }
    }

    public void add(BrewingStand brewingstand) {
        synchronized (standlocations) {
            if (!standlocations.containsKey(brewingstand.location().chunklocation())) {
                standlocations.put(brewingstand.location().chunklocation(), new HashMap<>());
            }
            Map<Location, BrewingStand> locations = standlocations.get(brewingstand.location().chunklocation());
            synchronized (locations) {
                locations.put(brewingstand.location(), brewingstand);
            }
        }
        synchronized (standuuids) {
            standuuids.put(brewingstand.uuid, brewingstand);
        }
    }

    public void chunkload(ChunkLocation chunklocation) {
        boolean containschunk;
        synchronized (standlocations) { containschunk = standlocations.containsKey(chunklocation); }
        if(!containschunk) {
            WorldSaver saver = saver(chunklocation.world());
            Set<BrewingStand> stands = null;
            synchronized (saver) { stands = saver.get(chunklocation.x(), chunklocation.z(), BrewingStand.class); }
            for(BrewingStand brewingstand : stands) {
                brewingstand.initialize();
                add(brewingstand);
            }
        }

        changestate(chunklocation, true);
    }

    public void chunkunload(ChunkLocation chunklocation) {
        changestate(chunklocation, false);
    }

    private void changestate(ChunkLocation chunk, boolean enable) {
        Map<Location, BrewingStand> chunkstands;
        synchronized (standlocations) { chunkstands = standlocations.get(chunk); }
        if(chunkstands == null) {
            return;
        }

        synchronized (chunkstands) {
            for (Map.Entry<Location, BrewingStand> entry : chunkstands.entrySet()) {
                BrewingStand brewingstand = entry.getValue();
                brewingstand.loaded(enable);
            }
        }
    }
}
