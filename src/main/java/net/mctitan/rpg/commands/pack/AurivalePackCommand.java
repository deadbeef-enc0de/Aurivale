package net.mctitan.rpg.commands.pack;

import net.mctitan.rpg.commands.AurivaleBaseCommand;

public class AurivalePackCommand extends AurivaleBaseCommand {

    public AurivalePackCommand() {
        super("pack");

        this.add(new AurivalePackCreateCommand());
        this.add(new AurivalePackGenmodsCommand());
        this.add(new AurivalePackModifierCommand());
    }
}
