package net.mctitan.rpg.brewing.recipe;

import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.BrewingRecipeType;
import net.mctitan.rpg.enums.EnchanterType;
import org.bukkit.Material;

public class EnchanterBrewingRecipe extends BrewingRecipe {
    private transient EnchanterType enchanter;

    public EnchanterBrewingRecipe() {}

    public EnchanterBrewingRecipe(BrewingRecipeType type, String name, int fuelcost, int brewtime,
                                  EnchanterType enchanter) {
        super(type, name, fuelcost, brewtime);
        this.enchanter = enchanter;
    }

    public EnchanterType enchanter() { return enchanter; }

    @Override
    public boolean matches(ItemStack potion) {
        // make sure that item stack is a craftable potion
        return potion.craftable() != null &&
                (potion.bukkitstack().getType() == Material.POTION ||
                 potion.bukkitstack().getType() == Material.SPLASH_POTION ||
                 potion.bukkitstack().getType() == Material.LINGERING_POTION);
    }

    @Override
    public ItemStack apply(Player player, ItemStack stack, int skill) {
        // apply the enchanter to the item and return the item
        return enchanter.enchanter().brew(stack, skill);
    }
}
