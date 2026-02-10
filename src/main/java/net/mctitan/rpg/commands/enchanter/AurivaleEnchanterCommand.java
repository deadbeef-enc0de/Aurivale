package net.mctitan.rpg.commands.enchanter;

import net.mctitan.rpg.commands.AurivaleBaseCommand;

public class AurivaleEnchanterCommand extends AurivaleBaseCommand {
    public AurivaleEnchanterCommand() {
        super("enchanter");

        this.add(new AurivaleEnchanterApplyCommand());
        this.add(new AurivaleEnchanterGiveCommand());
    }
}
