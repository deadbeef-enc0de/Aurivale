package net.mctitan.rpg.commands.modifier;

import net.mctitan.rpg.commands.AurivaleBaseCommand;

public class AurivaleModifierCommand extends AurivaleBaseCommand {
    public AurivaleModifierCommand() {
        super("modifier");

        this.add(new AurivaleModifierAddCommand());
        this.add(new AurivaleModifierInfoCommand());
        this.add(new AurivaleModifierRemoveCommand());
        this.add(new AurivaleModifierSlotsModifier());
    }
}
