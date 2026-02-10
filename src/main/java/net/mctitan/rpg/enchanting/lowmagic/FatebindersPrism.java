package net.mctitan.rpg.enchanting.lowmagic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.tables.tagged.selectors.PrefixSelector;
import net.mctitan.rpg.data.tables.tagged.selectors.SuffixSelector;
import net.mctitan.rpg.data.tables.weighted.WeightedTable;
import net.mctitan.rpg.enchanting.Enchanter;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.enums.ItemType;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.unique.Unique;
import net.mctitan.rpg.unique.Uniques;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Random;

public class FatebindersPrism extends Enchanter {
    WeightedTable<ItemType> itemtypes = new WeightedTable<>();

    public FatebindersPrism() {
        itemtypes.insert(ItemType.MAGIC, 900);
        itemtypes.insert(ItemType.RARE, 90);
        itemtypes.insert(ItemType.UNIQUE, 10);
    }

    @Override
    public Component[] help() {
        return new Component[] {
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>This item will turn a <white>Normal<dark_aqua> item into a <blue>Magic<dark_aqua>, <yellow>Rare<dark_aqua>,"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>or <gold>Unique<dark_aqua> item randomly. <blue>Magic<dark_aqua> and <yellow>Rare<dark_aqua> items will"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>have the maximum amount of modifiers added."),
MiniMessage.miniMessage().deserialize("<!italic><gold>Uniques<dark_aqua> will be from the same craftable group")
        };
    }

    @Override public Component[] modhelp() {
        return new Component[] {
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>The above modifier will be added to the item if it"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>is valid for the type of item this is used on.")
        };
    }

    @Override public int modeldata() { return 10103; }
    @Override public EnchanterType type() { return EnchanterType.FATEBINDERS_PRISM; }

    @Override public boolean enchant(ItemStack enchanter, ItemStack stack, Random random, int skill) {
        throw new NotImplementedException();
    }

    @Override public boolean enchant(Player player, ItemStack enchanter, ItemStack stack, Random random, int skill) {
        // make sure the item is a normal item
        if(stack.immutable() || stack.type() != ItemType.NORMAL) {
            return false;
        }

        // get the enchantment modifier
        ModifierConfig enchantermod = enchanter.enchantermod();

        // get the type and then figure out what to do with that
        ItemType type = itemtypes.get(skill);
        if(type == ItemType.UNIQUE && Uniques.instance().has(stack.craftable().group())) {
            // get unique and set item in slot
            Unique unique = Uniques.instance().get(stack.craftable().group()).get(skill);
            stack.inventory().setItem(stack.slot(), unique.create().bukkitstack());
        } else {
            // if we rolled unique but there is not one, downgrade to rare and increase skill roll
            if(type == ItemType.UNIQUE) { type = ItemType.RARE; }

            // set type and add modiiers
            stack.type(type);
            stack.addmodifiers(enchantermod, PrefixSelector.selector(), random, type.prefixes(), skill);
            stack.addmodifiers(enchantermod, SuffixSelector.selector(), random, type.suffixes(), skill);
            stack.addenchantment(Enchantment.mending);
        }

        return true;
    }
}
