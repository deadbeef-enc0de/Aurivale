package net.mctitan.rpg.crafting.recipes;

import net.mctitan.rpg.crafting.Craftable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.Map;

public class Shaped extends Recipe {
    private String[] shape;
    private ShapedRecipe recipe = null;

    public Shaped(Craftable craftable, ConfigurationSection section) {
        super(craftable, section);

        shape = section.getStringList("shape").toArray(new String[0]);
    }

    public org.bukkit.inventory.Recipe bukkitrecipe() {
        if(recipe == null) {
            recipe = new ShapedRecipe(key(), result().bukkitstack());
            recipe.shape(shape);
            Map<Character, RecipeChoice> materials = materials();
            for(Character c : materials.keySet()) {
                recipe.setIngredient(c, materials.get(c));
            }
        }

        return recipe;
    }

    public Map<RecipeChoice.MaterialChoice, Integer> materialcount() {
        Map<RecipeChoice.MaterialChoice, Integer> materials = new HashMap<>();
        for(String line : shape) {
            for(char c : line.toCharArray()) {
                RecipeChoice.MaterialChoice choice = (RecipeChoice.MaterialChoice)materials().get(c);
                if(choice == null) { continue; }
                if(!materials.containsKey(choice)) { materials.put(choice, 0); }
                materials.put(choice, materials.get(choice) + 1);
            }
        }

        return materials;
    }
}
