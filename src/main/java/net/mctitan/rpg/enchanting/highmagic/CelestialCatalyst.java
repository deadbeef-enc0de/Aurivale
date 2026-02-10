package net.mctitan.rpg.enchanting.highmagic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.tables.tagged.selectors.PrefixSelector;
import net.mctitan.rpg.data.tables.tagged.selectors.SuffixSelector;
import net.mctitan.rpg.enchanting.Enchanter;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.enums.ItemType;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.util.Math;

import java.util.Random;

public class CelestialCatalyst extends Enchanter {
    @Override public Component[] help() {
        return new Component[] {
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>This item will turn a <white>Normal<dark_aqua> item into a <yellow>Rare<dark_aqua> item."),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>Adding 2-3 prefix modifiers and 2-3 suffix modifiers"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>with a minimum of 5 modifiers added.")
        };
    }

    @Override public Component[] modhelp() {
        return new Component[] {
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>The above modifier will be added to the item if it"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>is valid for the type of item this is used on.")
        };
    }

    @Override public int modeldata() { return 10202; }
    @Override public EnchanterType type() { return EnchanterType.CELESTIAL_CATALYST; }

    @Override public boolean enchant(ItemStack enchanter, ItemStack stack, Random random, int skill) {
        // make sure the item is a normal item
        if (stack.immutable() || stack.type() != ItemType.NORMAL) {
            return false;
        }

        // get the enchantment modifier
        ModifierConfig enchantermod = enchanter.enchantermod();

        // get number of prefixes and suffixes
        int modifiers = Math.nextInt(2)+5;
        int prefixes = Math.nextInt(2)+2;
        if(modifiers == 6) { prefixes = 3; }
        int suffixes = modifiers - prefixes;

        // set the type, add the modifiers, add enchantment sheen
        stack.type(ItemType.RARE);
        stack.addmodifiers(enchantermod, PrefixSelector.selector(), random, prefixes, skill);
        stack.addmodifiers(enchantermod, SuffixSelector.selector(), random, suffixes, skill);
        stack.addenchantment(Enchantment.mending);

        return true;
    }
}
