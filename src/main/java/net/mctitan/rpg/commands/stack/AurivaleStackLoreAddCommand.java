package net.mctitan.rpg.commands.stack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.commands.arguments.Argument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.LoreSlot;

import java.util.Arrays;

public class AurivaleStackLoreAddCommand extends AurivaleBaseCommand {
    private final Argument<LoreSlot> slot = new Argument<>("prefix", LoreSlot.class);
    private final Argument<String> lore = new Argument<>("lore", String.class);

    public AurivaleStackLoreAddCommand() {
        super("add");

        this.add(slot);
        this.add(lore);
    }

    public boolean runCommand(Player player, String[] args) {
        ItemStack inhand = new ItemStack(player.bukkitplayer().getInventory(), player.bukkitplayer().getInventory().getHeldItemSlot());
        String lorestr = "<!italic>"+String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Component lore = MiniMessage.miniMessage().deserialize(lorestr);
        inhand.addlore(this.slot.value(), lore);

        return true;
    }
}
