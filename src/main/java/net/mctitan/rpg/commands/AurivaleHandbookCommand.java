package net.mctitan.rpg.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.handbook.Handbook;
import org.bukkit.inventory.ItemStack;

public class AurivaleHandbookCommand extends AurivaleBaseCommand {
    private final Handbook handbook = Handbook.instance();

    public AurivaleHandbookCommand() {
        super("handbook");
    }

    public boolean runCommand(Player player) {
        // make sure the player doesn't already have a handbook
        for(ItemStack stack : player.bukkitplayer().getInventory().getContents()) {
            if(handbook.item().bukkitstack().equals(stack)) {
                player.bukkitplayer().sendMessage(Component.text("You already have a handbook").color(NamedTextColor.RED));
                return true;
            }
        }

        // give player the handbook and update the players handbook version
        player.bukkitplayer().getInventory().addItem(handbook.item().bukkitstack());
        player.handbook(handbook.version());

        return true;
    }
}
