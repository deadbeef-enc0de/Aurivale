package net.mctitan.rpg.commands.stack;

import net.mctitan.rpg.commands.AurivaleBaseCommand;

public class AurivaleStackCommand extends AurivaleBaseCommand {
    public AurivaleStackCommand() {
        super("stack");

        this.add(new AurivaleStackGiveCommand());
        this.add(new AurivaleStackLoreCommand());
        this.add(new AurivaleStackPrintCommand());
    }
}
