package net.mctitan.rpg.commands.stack;

import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.util.Logger;

import java.util.logging.Level;

public class AurivaleStackPrintCommand extends AurivaleBaseCommand implements Logger {
    public AurivaleStackPrintCommand() {
        super("print");
    }

    public boolean runCommand(Player player) {
        ItemStack inhand = new ItemStack(player.bukkitplayer().getInventory(), player.bukkitplayer().getInventory().getHeldItemSlot());
        log(Level.INFO, inhand.bukkitstack().serialize().toString());

        return true;
    }
}
