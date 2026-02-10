package net.mctitan.rpg.commands.unique;

import net.mctitan.commands.arguments.Argument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.commands.completers.UniqueCompleter;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.unique.Unique;

public class AurivaleUniqueGiveCommand extends AurivaleBaseCommand {
    private final Argument<Unique> unique = new Argument<>("unique", Unique.class);

    public AurivaleUniqueGiveCommand() {
        super("give");

        this.unique.completer(new UniqueCompleter());

        this.add(unique);
    }

    public boolean runCommand(Player player) {
        Unique unique = this.unique.value();
        if(unique == null) {
            return true;
        }

        ItemStack stack = unique.create();
        player.bukkitplayer().getInventory().addItem(stack.bukkitstack());

        return true;
    }
}
