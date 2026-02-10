package net.mctitan.rpg.enchanting.lowmagic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enchanting.Enchanter;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.enums.ItemType;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.modifier.ModifierConfig;

import java.util.Random;

public class CharmOfEnrichment extends Enchanter {
    private static final int PREFIXES = ItemType.MAGIC.prefixes();
    private static final int SUFFIXES = ItemType.MAGIC.suffixes();

    @Override public Component[] help() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>This item will add a modifier to a <blue>Magic<dark_aqua> item."),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>To a maximum of "+PREFIXES+" prefixes and "+SUFFIXES+" suffixes.")
        };
    }

    @Override public Component[] modhelp() {
        return new Component[] {
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>The above modifier will be added to the item if it"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>the item does not already have the modifier.")
        };
    }

    @Override public int modeldata() { return 10102; }
    @Override public EnchanterType type() { return EnchanterType.CHARM_OF_ENRICHMENT; }

    @Override public boolean enchant(ItemStack enchanter, ItemStack stack, Random random, int skill) {
        // make sure the item is a normal item
        if (stack.immutable() || stack.type() != ItemType.MAGIC) {
            return false;
        }

        // make sure that there are less than 2 prefixes and 2 suffixes
        if(stack.modifiers(ModifierSlot.PREFIX).size() >= PREFIXES && stack.modifiers(ModifierSlot.SUFFIX).size() >= SUFFIXES) {
            return false;
        }

        // get the enchantment modifier
        ModifierConfig enchantermod = enchanter.enchantermod();

        // add a modifier
        stack.addmodifiers(enchantermod, random, 1, skill);

        return true;
    }
}
