package net.mctitan.rpg.enchanting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.enums.LoreSlot;
import net.mctitan.rpg.util.Item;
import net.mctitan.rpg.util.Math;
import org.bukkit.Material;

import java.util.Random;

public abstract class Enchanter {
    private ItemStack stack;

    public ItemStack stack() {
        if(stack == null) {
            // make a clean stack
            stack = Item.cleanstack(Material.PAPER);

            // add help lore
            for(Component help : help()) { stack.addlore(LoreSlot.HEADER, help); }
            for(Component apply : apply()) { stack.addlore(LoreSlot.UNIQUE, apply); }

            // set data on the item stack
            stack.displayname(Component.text(type().string()).color(NamedTextColor.GOLD));
            stack.enchantertype(type());
            stack.modeldata(modeldata());
        }
        return stack.clone();
    }

    public Component[] apply() {
        return new Component[] {
MiniMessage.miniMessage().deserialize("<!italic><dark_gray>(Right click this item with your character inventory"),
MiniMessage.miniMessage().deserialize("<!italic><dark_gray>open, then left click another item to apply.)"),
        };
    }

    public abstract Component[] help();
    public abstract Component[] modhelp();
    public abstract int modeldata();
    public abstract EnchanterType type();
    public abstract boolean enchant(ItemStack enchanter, ItemStack stack, Random random, int skill);

    public final ItemStack brew(ItemStack stack, int skill) { return brew(stack, Math.random(), skill); }
    public ItemStack brew(ItemStack stack, Random random, int skill) { enchant(stack, random, skill); return stack; }
    public boolean enchant(ItemStack stack, int skill) { return enchant(stack, Math.random(), skill); }
    public boolean enchant(ItemStack stack, Random random, int skill) { return enchant(this.stack(), stack, random, skill); }

    public final boolean enchant(Player player, ItemStack enchanter, ItemStack stack, int skill) {
        return enchant(player, enchanter, stack, Math.random(), skill);
    }

    public boolean enchant(Player player, ItemStack enchanter, ItemStack stack, Random random, int skill) {
        return enchant(enchanter, stack, Math.random(), skill);
    }
}
