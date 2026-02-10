package net.mctitan.rpg.brewing.recipe;

import net.mctitan.data.BasicData;
import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.crafting.Craftables;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.BrewingRecipeType;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.util.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public class BrewingRecipe extends BasicData implements Logger {
    private BrewingRecipeType type;
    private String name;
    private transient int fuelcost;
    private transient int brewtime;

    public BrewingRecipe() {}

    public BrewingRecipe(BrewingRecipeType type, String name, int fuelcost, int brewtime) {
        this.type = type;
        this.name = name;
        this.fuelcost = fuelcost;
        this.brewtime = brewtime;
    }

    public BrewingRecipeType type() { return type; }
    public String name() { return name; }
    public int fuelcost() { return fuelcost; }
    public int brewtime() { return brewtime; }

    public boolean matches(ItemStack potion) {
        throw new RuntimeException("Not implemented");
    }

    public ItemStack apply(Player player, ItemStack stack, int skill) {
        throw new RuntimeException("Not implemented");
    }

    public static BrewingRecipe recipe(ConfigurationSection config) {
        String name = config.getName();
        BrewingRecipeType type = BrewingRecipeType.valueOf(config.getString("type"));
        int fuelcost = config.getInt("fuelcost");
        int brewtime = config.getInt("brewtime");

        switch(type) {
            case UPGRADE -> {
                Material ingredient = Material.getMaterial(config.getString("ingredient"));
                Craftable from = Craftables.instance().craftable(config.getString("from"));
                Craftable to = Craftables.instance().craftable(config.getString("to"));
                return new UpgradeBrewingRecipe(type, name, fuelcost, brewtime, ingredient, from, to);
            }
            case CHANGE -> {
                Material ingredient = Material.getMaterial(config.getString("ingredient"));
                Craftable from = Craftables.instance().craftable(config.getString("from"));
                Craftable to = Craftables.instance().craftable(config.getString("to"));
                return new ChangeBrewingRecipe(type, name, fuelcost, brewtime, ingredient, from, to);
            }
            case ENCHANTER -> {
                EnchanterType enchantertype = EnchanterType.valueOf(config.getString("enchanter"));
                return new EnchanterBrewingRecipe(type, name, fuelcost, brewtime, enchantertype);
            }
        }

        Logger.LOG(Level.SEVERE, String.format("BrewingRecipe type=%s has no implementation", type.name()));
        return null;
    }
}
