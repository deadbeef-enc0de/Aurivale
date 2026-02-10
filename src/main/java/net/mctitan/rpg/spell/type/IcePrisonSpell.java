package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.spell.instance.IcePrisonInstance;
import net.mctitan.rpg.spell.instance.SpellInstance;

public class IcePrisonSpell extends Spell {
    private double radiusflat;
    private double radiusbase;

    @Override
    public void initialize() {
        super.initialize();

        radiusflat = section().getDouble("radius.flat");
        radiusbase = section().getDouble("radius.base");
    }

    public int radius(int level) { return (int)Math.round(radiusflat * Math.pow(radiusbase, level - 1)); }

    public String name() { return "Ice Prison"; }
    public String configname() { return "ice_prison"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new IcePrisonInstance(id, level, duration(level), caster, target);
    }
}
