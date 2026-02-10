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

public class EssenceShard extends Enchanter {
    @Override public Component[] help() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>This item will imprint an <gold>Enchanter<dark_aqua> with a modifier.")
        };
    }

    @Override public Component[] modhelp() {
        return new Component[]{
MiniMessage.miniMessage().deserialize("<!italic><dark_aqua>The above modifier will be imprinted on the <gold>Enchanter<dark_aqua>.")
        };
    }

    @Override public int modeldata() { return 10304; }
    @Override public EnchanterType type() { return EnchanterType.ESSENCE_SHARD; }

    @Override public boolean enchant(ItemStack enchanter, ItemStack stack, Random random, int skill) {
        throw new NotImplementedException();
    }

    @Override public boolean enchant(Player player, ItemStack enchanter, ItemStack stack, Random random, int skill) {
        if(stack.immutable() || stack.enchantertype() == null || stack.enchantertype().meta() || !stack.enchantertype().modable()) {
            return false;
        }

        int amount = stack.bukkitstack().getAmount();
        if(amount == 1) {
            stack.enchantermod(enchanter.enchantermod());
        } else {
            // clone stack
            ItemStack cursor = stack.clone();

            // remove 1 from stack and make new stack have 1
            stack.bukkitstack().setAmount(amount - 1);
            cursor.bukkitstack().setAmount(1);

            // set enchanter modifier
            cursor.enchantermod(enchanter.enchantermod());

            // set item on cursor and reset enchanting state
            player.bukkitplayer().setItemOnCursor(cursor.bukkitstack());
            player.enchantingstate(EnchantingState.NOT_ENCHANTING);
        }

        return true;
    }
}
