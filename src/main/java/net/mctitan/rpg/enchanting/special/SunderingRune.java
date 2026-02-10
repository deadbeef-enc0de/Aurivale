package net.mctitan.rpg.enchanting.special;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enchanting.Enchanter;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.enums.EnchantingState;
import net.mctitan.rpg.enums.ItemType;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.Modifiers;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Random;

public class SunderingRune extends Enchanter {
    @Override public Component[] help() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>This item will destroy a <yellow>Rare<dark_aqua> item giving the user"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>an <gold>Essence Shard<dark_aqua> enchanter with a modifier from"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>the item destroyed imprinted on it.")
        };
    }

    @Override public Component[] modhelp() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><red>NOT IMPLEMENTED!")
        };
    }

    @Override public int modeldata() { return 10303; }
    @Override public EnchanterType type() { return EnchanterType.SUNDERING_RUNE; }

    @Override public boolean enchant(ItemStack enchanter, ItemStack stack, Random random, int skill) {
        throw new NotImplementedException();
    }

    @Override public ItemStack brew(ItemStack stack, Random random, int skill) {
        // make sure item is good to be enchanted
        if(stack.immutable() || stack.type() != ItemType.RARE) {
            return stack;
        }

        // make sure there is an inventory associated with the input stack
        if(stack.inventory() == null || stack.slot() == -1) {
            return stack;
        }

        // get the result and set the slot
        return result(stack, random);
    }

    @Override public boolean enchant(Player player, ItemStack enchanter, ItemStack stack, Random random, int skill) {
        if(stack.immutable() || stack.type() != ItemType.RARE) {
            return false;
        }

        // get essence shard
        ItemStack essenceshard = result(stack, random);

        // set item on cursor
        player.bukkitplayer().setItemOnCursor(essenceshard.bukkitstack());
        player.enchantingstate(EnchantingState.NOT_ENCHANTING);

        // remove old item from inventory
        player.bukkitplayer().getInventory().remove(stack.bukkitstack());

        return true;
    }

    private ItemStack result(ItemStack stack, Random random) {
        // get list of prefix and suffix modifiers on item and pick one randomly
        List<Modifier> modifiers = stack.modifiers(ModifierSlot.PREFIX, ModifierSlot.SUFFIX);
        Modifier modifier = modifiers.get(random.nextInt(modifiers.size()));
        ModifierConfig modifierconfig = Modifiers.instance().config(modifier.id());

        // Get an essence shard stack and add enchanter mod to the item
        ItemStack essenceshard = EnchanterType.ESSENCE_SHARD.enchanter().stack();
        essenceshard.enchantermod(modifierconfig);

        // return essence shard
        return essenceshard;
    }
}
