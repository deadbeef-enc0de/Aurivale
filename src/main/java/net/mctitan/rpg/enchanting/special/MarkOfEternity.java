package net.mctitan.rpg.enchanting.special;

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

public class MarkOfEternity extends Enchanter {
    @Override public Component[] help() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>This item will take a <yellow>Rare<dark_aqua> without an <aqua>Implicit<dark_aqua> modifier"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>and pick a prefix or suffix modifier at random then"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>move that modifier to an <aqua>Implicit<dark_aqua> modifier slot.")
        };
    }

    @Override public Component[] modhelp() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>The above modifier will not be moved to an <aqua>Implicit<dark_aqua>"),
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>modifier slot.")
        };
    }

    @Override public int modeldata() { return 10302; }
    @Override public EnchanterType type() { return EnchanterType.MARK_OF_ETERNITY; }

    @Override public boolean enchant(ItemStack enchanter, ItemStack stack, Random random, int skill) {
        if(stack.immutable() || stack.type() != ItemType.RARE || !stack.modifiers(ModifierSlot.IMPLICIT).isEmpty()) {
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

        // move modifier from slot to implicit
        Modifier move = modifiers.get(random.nextInt(modifiers.size()));
        move.unapply(stack);
        move.apply(ModifierSlot.IMPLICIT, stack);

        return true;
    }
}
