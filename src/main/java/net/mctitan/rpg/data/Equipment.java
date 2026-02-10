package net.mctitan.rpg.data;

import net.mctitan.rpg.enums.ItemSlot;
import net.mctitan.rpg.modifier.Modifier;

import java.util.HashMap;
import java.util.Map;

public class Equipment {
    private Map<ItemSlot, ItemStack> equipment = new HashMap<>();

    public ItemStack item(ItemSlot slot) { return this.equipment.get(slot); }
    public void item(ItemSlot slot, ItemStack stack) { this.equipment.put(slot, stack); }

    public void resetmodifiers(Player player) {
        for(ItemStack stack : equipment.values()) {
            if(stack == null )  { continue; }
            for(Modifier modifier : stack.modifiers()) {
                modifier.unapply(player);
                modifier.apply(player);
            }
        }
    }
}
