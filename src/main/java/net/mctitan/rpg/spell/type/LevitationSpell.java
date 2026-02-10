package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.spell.instance.LevitationInstance;
import net.mctitan.rpg.spell.instance.SpellInstance;

public class LevitationSpell extends Spell {
    private double levelflat;
    private double levelbase;

    @Override
    public void initialize() {
        super.initialize();

        levelflat = section().getDouble("level.flat");
        levelbase = section().getDouble("level.base");
    }

    public int level(int level) { return (int)Math.round(levelflat * Math.pow(levelbase, level - 1)); }

    public String name() { return "Levitation"; }
    public String configname() { return "levitation"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new LevitationInstance(id, level, duration(level), caster, target);
    }
}
