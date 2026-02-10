package net.mctitan.rpg.brewing.recipe;

import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.BrewingRecipeType;
import net.mctitan.rpg.util.Key;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataType;

public class ChangeBrewingRecipe extends BrewingRecipe {
    private transient Material ingredient;
    private transient Craftable from;
    private transient Craftable to;

    public ChangeBrewingRecipe() {}

    public ChangeBrewingRecipe(BrewingRecipeType type, String name, int fuelcost, int brewtime,
                               Material ingredient, Craftable from, Craftable to) {
        super(type, name, fuelcost, brewtime);
        this.ingredient = ingredient;
        this.from = from;
        this.to = to;
    }

    public Material getIngredient() { return ingredient; }
    public Craftable from() { return from; }
    public Craftable to() { return to; }

    @Override
    public boolean matches(ItemStack potion) {
        return from == potion.craftable();
    }

    @Override
    public ItemStack apply(Player player, ItemStack stack, int skill) {
        // make sure the stack is the correct craftable
        if(stack.craftable() != from) {
            // return the original item
            return stack;
        }

        // return new item stack of new type with same crafting value
        int craftroll = stack.get(Key.CRAFT_VALUE_KEY, PersistentDataType.INTEGER);
        return to.create(craftroll);
    }
}
