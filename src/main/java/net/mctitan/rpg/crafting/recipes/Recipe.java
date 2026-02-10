package net.mctitan.rpg.crafting.recipes;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.data.ItemStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.RecipeChoice;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class Recipe {
    private NamespacedKey key;
    private ItemStack result;
    private Map<Character, RecipeChoice> materials = new HashMap<>();

    public Recipe(Craftable craftable, ConfigurationSection section) {
        key = new NamespacedKey(Aurivale.instance(), String.format("%s_%s", craftable.key().getKey(), section.getName()));
        result = craftable.template();

        ConfigurationSection materialssection = section.getConfigurationSection("materials");
        for(String choice : materialssection.getKeys(false)) {
            char c = choice.charAt(0);
            List<Material> materials = new LinkedList<>();
            for(String materialstr : materialssection.getStringList(choice)) {
                materials.add(Material.valueOf(materialstr));
            }
            this.materials.put(c, new RecipeChoice.MaterialChoice(materials.toArray(new Material[0])));
        }
    }

    public NamespacedKey key() { return key; }
    public ItemStack result() { return result; }
    public Map<Character, RecipeChoice> materials() { return Map.copyOf(materials); }
    public abstract org.bukkit.inventory.Recipe bukkitrecipe();
    public abstract Map<RecipeChoice.MaterialChoice, Integer> materialcount();
}
