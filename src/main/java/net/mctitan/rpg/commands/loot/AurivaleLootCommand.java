package net.mctitan.rpg.commands.loot;

import net.mctitan.rpg.commands.AurivaleBaseCommand;

public class AurivaleLootCommand extends AurivaleBaseCommand {
    public AurivaleLootCommand() {
        super("loot");

        this.add(new AurivaleLootDropCommand());
    }
}
