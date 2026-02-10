package net.mctitan.rpg.enchanting.highmagic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enchanting.Enchanter;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.enums.ItemType;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OrbOfFlux extends Enchanter {
    @Override public Component[] help() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>This item will re-roll the values of all modifiers"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>on a <blue>Magic<dark_aqua> or <yellow>Rare<dark_aqua> item.")
        };
    }

    @Override public Component[] modhelp() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>The above modifier will not have it's values re-rolled.")
        };
    }

    @Override public int modeldata() { return 10205; }
    @Override public EnchanterType type() { return EnchanterType.ORB_OF_FLUX; }

    @Override public boolean enchant(ItemStack enchanter, ItemStack stack, Random random, int skill) {
        if(stack.immutable() || stack.type() != ItemType.MAGIC && stack.type() != ItemType.RARE) {
            return false;
        }

        // get the enchantment modifier
        ModifierConfig enchantermod = enchanter.enchantermod();

        // get all modifiers from the item
        Map<ModifierSlot, List<Modifier>> modifiers = new HashMap<>();
        for(ModifierSlot slot : ModifierSlot.values()) {
            modifiers.put(slot, stack.modifiers(slot));
        }

        // remove modifiers and add a new one of their template
        for(ModifierSlot slot : ModifierSlot.values()) {
            for(Modifier modifier : modifiers.get(slot)) {
                modifier.unapply(slot, stack);
                if(enchantermod != null && enchantermod.id().equals(modifier.id())) {
                    stack.addmodifier(slot, modifier);
                } else {
                    stack.addmodifier(slot, modifier.rawtemplate().modifier());
                }
            }
        }

        return true;
    }
}
