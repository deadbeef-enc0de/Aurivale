package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.spell.instance.TeleportInstance;
import net.mctitan.rpg.spell.instance.SpellInstance;

public class TeleportSpell extends Spell {
    private double distanceflat;
    private double distancebase;

    @Override
    public void initialize() {
        super.initialize();

        distanceflat = section().getDouble("distance.flat");
        distancebase = section().getDouble("distance.base");
    }

    public int distance(int level) { return (int)Math.round(distanceflat * Math.pow(distancebase, level - 1)); }

    public String name() { return "Teleport"; }
    public String configname() { return "teleport"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new TeleportInstance(id, level, caster, target);
    }
}
