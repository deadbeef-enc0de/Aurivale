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

import java.util.List;
import java.util.Random;

public class VoidtearSigil extends Enchanter {
    @Override public Component[] help() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>This item will remove a modifier from a <blue>Magic<dark_aqua> or <yellow>Rare<dark_aqua> item."),
        };
    }

    @Override public Component[] modhelp() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>The above modifier will not be removed from the item.")
        };
    }

    @Override public int modeldata() { return 10204; }
    @Override public EnchanterType type() { return EnchanterType.VOIDTEAR_SIGIL; }

    @Override public boolean enchant(ItemStack enchanter, ItemStack stack, Random random, int skill) {
        if (stack.immutable() || stack.type() != ItemType.MAGIC &&stack.type() != ItemType.RARE) {
            return false;
        }

        // get the enchantment modifier
        ModifierConfig enchantermod = enchanter.enchantermod();

        // get modifiers on item filter the enchantment modifier
        List<Modifier> modifiers = stack.modifiers(ModifierSlot.PREFIX, ModifierSlot.SUFFIX)
                .stream()
                .filter(mod -> (enchantermod == null) || !enchantermod.id().equals(mod.id()))
                .toList();
        if(modifiers.isEmpty()) {
            return false;
        }

        // remove modifier from item
        Modifier remove = modifiers.get(random.nextInt(modifiers.size()));
        remove.unapply(stack);

        return true;
    }
}
