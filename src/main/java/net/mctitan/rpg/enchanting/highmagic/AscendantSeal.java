package net.mctitan.rpg.enchanting.highmagic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.tables.tagged.selectors.PrefixSelector;
import net.mctitan.rpg.data.tables.tagged.selectors.SuffixSelector;
import net.mctitan.rpg.enchanting.Enchanter;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.enums.ItemType;
import net.mctitan.rpg.modifier.ModifierConfig;

import java.util.Random;

public class AscendantSeal extends Enchanter {
    @Override public Component[] help() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>This item will turn a <blue>Magic<dark_aqua> item into a <yellow>Rare<dark_aqua> item."),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>Adding 1 prefix and 1 suffix.")
        };
    }

    @Override public Component[] modhelp() {
        return new Component[] {
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>The above modifier will be added to the item if it"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>is valid for the type of item this is used on and"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>the item does not already have the modifier.")
        };
    }

    @Override public int modeldata() { return 10201; }
    @Override public EnchanterType type() { return EnchanterType.ASCENDANT_SEAL; }

    @Override public boolean enchant(ItemStack enchanter, ItemStack stack, Random random, int skill) {
        // make sure the item is a magic item
        if(stack.immutable() || stack.type() != ItemType.MAGIC) {
            return false;
        }

        // get the enchantment modifier
        ModifierConfig enchantermod = enchanter.enchantermod();

        // set the type, add the modifiers
        stack.type(ItemType.RARE);
        stack.addmodifiers(enchantermod, PrefixSelector.selector(), random, 1, skill);
        stack.addmodifiers(enchantermod, SuffixSelector.selector(), random, 1, skill);

        return true;
    }
}
