package net.mctitan.rpg.commands.unique;

import net.mctitan.rpg.commands.AurivaleBaseCommand;

public class AurivaleUniqueCommand extends AurivaleBaseCommand {
    public AurivaleUniqueCommand() {
        super("unique");

        this.add(new AurivaleUniqueAddCommand());
        this.add(new AurivaleUniqueGiveCommand());
    }
}
