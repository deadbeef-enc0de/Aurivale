package net.mctitan.rpg.commands.pack;

import net.mctitan.rpg.commands.AurivaleBaseCommand;

public class AurivalePackModifierCommand extends AurivaleBaseCommand {
    public AurivalePackModifierCommand() {
        super("modifier");

        this.add(new AurivalePackModifierAddCommand());
        this.add(new AurivalePackModifierInfoCommand());
    }
}
