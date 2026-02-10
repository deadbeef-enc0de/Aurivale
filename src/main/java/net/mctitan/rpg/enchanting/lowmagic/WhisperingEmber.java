package net.mctitan.rpg.enchanting.lowmagic;

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

import java.util.Random;

public class WhisperingEmber extends Enchanter {
    @Override public Component[] help() {
        return new Component[] {
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>This item will turn a <white>Normal<dark_aqua> item into a <blue>Magic<dark_aqua> item."),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>Adding 1 prefix modifier and 1 suffix modifier.")
        };
    }

    @Override public Component[] modhelp() {
        return new Component[] {
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>The above modifier will be added to the item if it"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>is valid for the type of item this is used on.")
        };
    }

    @Override public int modeldata() { return 10101; }
    @Override public EnchanterType type() { return EnchanterType.WHISPERING_EMBER; }

    @Override public boolean enchant(ItemStack enchanter, ItemStack stack, Random random, int skill) {
        // make sure the item is a normal item
        if(stack.immutable() || stack.type() != ItemType.NORMAL) {
            return false;
        }

        // get the enchantment modifier
        ModifierConfig enchantermod = enchanter.enchantermod();

        // set the type, add the modifiers, add enchantment sheen
        stack.type(ItemType.MAGIC);
        stack.addmodifiers(enchantermod, PrefixSelector.selector(), random, 1, skill);
        stack.addmodifiers(enchantermod, SuffixSelector.selector(), random, 1, skill);
        stack.addenchantment(Enchantment.mending);

        return true;
    }
}
