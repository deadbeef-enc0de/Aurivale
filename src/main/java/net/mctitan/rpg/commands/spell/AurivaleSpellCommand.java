package net.mctitan.rpg.commands.spell;

import net.mctitan.rpg.commands.AurivaleBaseCommand;

public class AurivaleSpellCommand extends AurivaleBaseCommand {
    public AurivaleSpellCommand() {
        super("spell");

        this.add(new AurivaleSpellCastCommand());
        this.add(new AurivaleSpellWandCommand());
    }
}
