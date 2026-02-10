package net.mctitan.rpg.spell.type.templates;

import net.mctitan.rpg.spell.type.Spell;

public abstract class BoltSpell extends Spell {
    private double speed;

    @Override
    public void initialize() {
        super.initialize();

        speed = section().getDouble("speed");
    }

    public double speed() { return speed; }
}
