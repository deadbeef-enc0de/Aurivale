package net.mctitan.rpg.enchanting.special;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enchanting.Enchanter;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.enums.EnchantingState;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Random;

public class PhantomReflection extends Enchanter {
    @Override public Component[] help() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>This item will make an <dark_red>Immutable<dark_aqua> copy of an item.")
        };
    }

    @Override public Component[] modhelp() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><red>NOT IMPLEMENTED!")
        };
    }

    @Override public int modeldata() { return 10301; }
    @Override public EnchanterType type() { return EnchanterType.PHANTOM_REFLECTION; }

    @Override public boolean enchant(ItemStack enchanter, ItemStack stack, Random random, int skill) {
        throw new NotImplementedException();
    }

    @Override public boolean enchant(Player player, ItemStack enchanter, ItemStack stack, Random random, int skill) {
        if(stack.immutable()) {
            return false;
        }

        // clone the item and seal it
        ItemStack clone = stack.clone();
        clone.setimmutable();

        // change the item on cursor to the clone and set player to not enchanting
        player.bukkitplayer().setItemOnCursor(clone.bukkitstack());
        player.enchantingstate(EnchantingState.NOT_ENCHANTING);

        return true;
    }
}
