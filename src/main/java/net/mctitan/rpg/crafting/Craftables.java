package net.mctitan.rpg.crafting;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.crafting.recipes.Recipe;
import net.mctitan.rpg.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

public class Craftables {
    private static final Craftables instance = new Craftables();
    private HashMap<String, Craftable> craftables = new HashMap<>();
    private HashSet<Material> materials = new HashSet<>();

    private Craftables() {}

    public static Craftables instance() { return instance; }
    public static void initialize() { instance.load(); }

    private void load() {
        // load all craftables
        Configuration config = Aurivale.instance().getConfig("craftables");
        for(String craftable : config.getKeys(false)) {
            ConfigurationSection craftablesection = config.getConfigurationSection(craftable);
            register(craftablesection);
        }

        // remove all recipes that share an output material with a craftable
        Iterator<org.bukkit.inventory.Recipe> recipes = Bukkit.recipeIterator();
        while(recipes.hasNext()) {
            org.bukkit.inventory.Recipe recipe = recipes.next();
            if(materials.contains(recipe.getResult().getType())) {
                Keyed keyed = (Keyed)recipe;
                Bukkit.removeRecipe(keyed.getKey());
            }
        }

        // add all craftable recipes
        for(Craftable craftable : craftables.values()) {
            for (Recipe recipe : craftable.recipes()) {
                Bukkit.addRecipe(recipe.bukkitrecipe());
            }
        }
    }

    public Set<String> names() { return craftables.keySet(); }
    public Craftable craftable(String keyname) { return craftables.get(keyname); }

    private void register(ConfigurationSection craftablesection) {
        try {
            Craftable craftable = new Craftable(craftablesection);
            if (craftable.template() != null) {
                Logger.LOG(Level.INFO, String.format("Registering craftable=%s slot=%s", craftablesection.getName(), craftable.itemslot()));
                craftables.put(craftable.key().getKey(), craftable);
                if(craftable.removerecipes()) {
                    materials.add(craftable.template().bukkitstack().getType());
                }
            }
        } catch (Exception e) {
            Logger.LOG(Level.SEVERE, String.format("Cannot load craftable=%s invalid config exception \"%s\"",
                    craftablesection.getName(),
                    e.getMessage()
            ));
        }
    }
}
