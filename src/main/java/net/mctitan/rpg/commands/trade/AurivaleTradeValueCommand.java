package net.mctitan.rpg.commands.trade;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.trading.Trading;

import java.util.ArrayList;
import java.util.List;

public class AurivaleTradeValueCommand extends AurivaleBaseCommand {
    public AurivaleTradeValueCommand() {
        super("value");
    }

    public boolean runCommand(Player player) {
        ItemStack stack = new ItemStack(player.bukkitplayer().getEquipment().getItemInMainHand());
        boolean enchanter = stack.enchantertype() != null;
        List<String> values = new ArrayList<>();
        int value = Trading.instance().value(stack);
        List<ItemStack> stacks = enchanter ? Trading.instance().materialstacks(value) : Trading.instance().enchanterstacks(value);
        for(ItemStack item : stacks) {
            String name = enchanter ? item.bukkitstack().getType().name() : item.enchantertype().name();
            values.add(String.format("%s x %d", name, item.bukkitstack().getAmount()));
        }

        player.bukkitplayer().sendMessage(Component.text(String.format("(%d) %s", value, String.join(", ", values))));

        return true;
    }
}
