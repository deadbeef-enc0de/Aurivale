package net.mctitan.rpg.util;

import net.mctitan.rpg.data.ItemStack;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;

public class Item {
    public static ItemStack cleanstack(Material material) { return cleanstack(material, 1); }
    public static ItemStack cleanstack(Material material, int amount) {
        // create the stack
        ItemStack item = new ItemStack(material, amount);

        // remove any built in attack speed
        item.addattribute(Attribute.ATTACK_SPEED, AttributeModifier.Operation.ADD_NUMBER, Key.NO_ATTACK_SPEED_KEY, 0, EquipmentSlot.HAND);

        // set item flags as needed
        item.flag(ItemFlag.HIDE_ATTRIBUTES, true);
        item.flag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, true);

        return item;
    }
}
