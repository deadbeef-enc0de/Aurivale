package net.mctitan.rpg.brewing.recipe;

import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.BrewingRecipeType;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.util.Key;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataType;

public class UpgradeBrewingRecipe extends BrewingRecipe {
    private transient Material ingredient;
    private transient Craftable from;
    private transient Craftable to;

    public UpgradeBrewingRecipe() {}

    public UpgradeBrewingRecipe(BrewingRecipeType type, String name, int fuelcost, int brewtime,
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

        // get upgraded item stack
        int craftroll = stack.get(Key.CRAFT_VALUE_KEY, PersistentDataType.INTEGER);
        ItemStack upgrade = to.create(craftroll);

        // move over all modifiers
        for(ModifierSlot slot : ModifierSlot.values()) {
            // don't copy over base modifiers
            if(slot == ModifierSlot.BASE) { continue; }

            // copy modifiers to new item
            for(Modifier modifier : stack.modifiers(slot)) {
                modifier.apply(slot, upgrade);
            }
        }

        // set type
        upgrade.type(stack.type());

        // return the new stack
        return upgrade;
    }
}
