package net.mctitan.rpg.commands.craftable;

import net.mctitan.rpg.commands.AurivaleBaseCommand;

public class AurivaleCraftableCommand extends AurivaleBaseCommand {
    public AurivaleCraftableCommand() {
        super("craftable");

        this.add(new AurivaleCraftableGiveCommand());
        this.add(new AurivaleCraftableTemplateCommand());
    }
}
