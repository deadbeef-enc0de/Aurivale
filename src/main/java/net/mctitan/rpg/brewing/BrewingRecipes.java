package net.mctitan.rpg.brewing;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.brewing.recipe.BrewingRecipe;
import net.mctitan.rpg.brewing.recipe.ChangeBrewingRecipe;
import net.mctitan.rpg.brewing.recipe.EnchanterBrewingRecipe;
import net.mctitan.rpg.brewing.recipe.UpgradeBrewingRecipe;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enums.BrewingStandSlot;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.util.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Level;

public class BrewingRecipes implements Logger {
    private static final BrewingRecipes instance = new BrewingRecipes();

    private Map<String, BrewingRecipe> recipenames = new HashMap<>();
    private Map<Material, Set<BrewingRecipe>> recipeingredients = new HashMap<>();
    private Map<EnchanterType, BrewingRecipe> recipeenchanters = new HashMap<>();

    private Map<Material, Integer> fuels = new HashMap<>();
    private Set<Material> ingredients = new HashSet<>();
    private Set<EnchanterType> enchanters = new HashSet<>();

    private BrewingRecipes() {}

    public static BrewingRecipes instance() { return instance; }
    public static void initialize() { instance.load(); }

    private void load() {
        Configuration brewingconfig = Aurivale.instance().getConfig("brewing");

        // load fuels from config
        ConfigurationSection fuelssection = brewingconfig.getConfigurationSection("fuels");
        for(String materialname : fuelssection.getKeys(false)) {
            Material material = Material.getMaterial(materialname);
            int time = fuelssection.getInt(materialname);
            if(material == null) {
                log(Level.WARNING, String.format("Fuel material=%s not found!", materialname));
                continue;
            }

            fuels.put(material, time);
        }

        // load all brewing recipes from config
        ConfigurationSection recipessection = brewingconfig.getConfigurationSection("recipes");
        for(String recipename : recipessection.getKeys(false)) {
            ConfigurationSection recipesection = recipessection.getConfigurationSection(recipename);
            BrewingRecipe recipe = BrewingRecipe.recipe(recipesection);
            if(recipe != null) {
                // add recipe
                log(Level.INFO, String.format("Loaded brewing recipe=%s", recipe.name()));
                recipenames.put(recipe.name(), recipe);

                // setup recipe maps, ingredients, and enchanter types
                switch(recipe.type()) {
                    case CHANGE -> {
                        Material ingredient = ((ChangeBrewingRecipe) recipe).getIngredient();
                        ingredients.add(ingredient);
                        if(!recipeingredients.containsKey(ingredient)) {
                            recipeingredients.put(ingredient, new HashSet<>());
                        }
                        recipeingredients.get(ingredient).add(recipe);
                    }
                    case ENCHANTER -> {
                        EnchanterType enchantertype = ((EnchanterBrewingRecipe) recipe).enchanter();
                        enchanters.add(enchantertype);
                        recipeenchanters.put(enchantertype, recipe);
                    }
                    case UPGRADE -> {
                        Material ingredient = ((UpgradeBrewingRecipe) recipe).getIngredient();
                        ingredients.add(ingredient);
                        if(!recipeingredients.containsKey(ingredient)) {
                            recipeingredients.put(ingredient, new HashSet<>());
                        }
                        recipeingredients.get(ingredient).add(recipe);
                    }
                }
            }
        }
    }

    public boolean has(String name) { return recipenames.containsKey(name); }
    public BrewingRecipe get(String name) { return recipenames.get(name); }

    public BrewingRecipe get(ItemStack ingredient, Set<ItemStack> potions) {
        if(ingredient.enchantertype() != null) {
            return get(ingredient.enchantertype(), potions);
        } else {
            return get(ingredient.bukkitstack().getType(), potions);
        }
    }

    public BrewingRecipe get(EnchanterType enchanter, Set<ItemStack> potions) {
        // get recipe
        BrewingRecipe recipe = recipeenchanters.get(enchanter);

        // make sure at least one potion matches the enchanter
        boolean matches = false;
        for(ItemStack potion : potions) {
            if(recipe.matches(potion)) {
                matches = true;
                break;
            }
        }
        if(!matches) { return null; }

        // return recipe
        return recipe;
    }

    public BrewingRecipe get(Material material, Set<ItemStack> potions)  {
        List<BrewingRecipe> recipes = new LinkedList<>();

        // go through all brewing recipes
        for(BrewingRecipe recipe : recipeingredients.get(material)) {
            for(ItemStack potion : potions) {
                // check if potion works with recipe
                if(recipe.matches(potion)) {
                    // add recipe to list
                    recipes.add(recipe);

                    // done checking recipe
                    break;
                }
            }
        }

        // make sure only one recipe was found
        if(recipes.size() == 1) {
            return recipes.stream().toList().getFirst();
        }

        return null;
    }

    public int fuel(Material material) {
        if(fuels.containsKey(material)) {
            return fuels.get(material);
        }
        return 0;
    }

    public boolean checkslot(ItemStack stack, BrewingStandSlot slot) {
        return findslot(stack).contains(slot);
    }

    public List<BrewingStandSlot> findslot(ItemStack stack) {
        // if the stack is a potion return the potions slots
        if(stack.bukkitstack().getType() == Material.POTION ||
           stack.bukkitstack().getType() == Material.SPLASH_POTION ||
           stack.bukkitstack().getType() == Material.LINGERING_POTION) {
            return List.of(BrewingStandSlot.POTION_LEFT, BrewingStandSlot.POTION_MIDDLE, BrewingStandSlot.POTION_RIGHT);
        }

        // if the stack is a fuel return the fuel slot
        if(fuels.containsKey(stack.bukkitstack().getType())) {
            return List.of(BrewingStandSlot.FUEL);
        }

        // if the slot is an ingredient
        if(ingredients.contains(stack.bukkitstack().getType()) ||
           enchanters.contains(stack.enchantertype())) {
            return List.of(BrewingStandSlot.INGREDIENT);
        }

        // as a backup return a non-stand slot
        return List.of(BrewingStandSlot.OTHER);
    }
}
