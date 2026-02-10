package net.mctitan.rpg.commands.craftable;

import net.mctitan.commands.arguments.Argument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.commands.completers.CraftableCompleter;
import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.data.Player;

public class AurivaleCraftableTemplateCommand extends AurivaleBaseCommand {
    private final Argument<Craftable> craftable = new Argument<>("craftable", Craftable.class);

    public AurivaleCraftableTemplateCommand() {
        super("template");

        this.craftable.completer(new CraftableCompleter());

        this.add(craftable);
    }

    public boolean runCommand(Player player) {
        Craftable craftable = this.craftable.value();
        if(craftable == null) {
            return true;
        }

        player.bukkitplayer().getInventory().addItem(craftable.template().bukkitstack());
        return true;
    }
}
