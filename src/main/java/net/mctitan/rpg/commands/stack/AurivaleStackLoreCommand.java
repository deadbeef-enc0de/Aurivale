package net.mctitan.rpg.commands.stack;

import net.mctitan.rpg.commands.AurivaleBaseCommand;

public class AurivaleStackLoreCommand extends AurivaleBaseCommand {
    public AurivaleStackLoreCommand() {
        super("lore");

        this.add(new AurivaleStackLoreAddCommand());
    }
}
