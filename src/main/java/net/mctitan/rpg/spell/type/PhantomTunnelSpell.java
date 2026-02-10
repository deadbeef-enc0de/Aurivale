package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.spell.instance.PhantomTunnelInstance;
import net.mctitan.rpg.spell.instance.SpellInstance;

public class PhantomTunnelSpell extends Spell {
    private double distanceflat;
    private double distancebase;

    @Override
    public void initialize() {
        super.initialize();

        distanceflat = section().getDouble("distance.flat");
        distancebase = section().getDouble("distance.base");
    }

    public double distance(int level) { return distanceflat * Math.pow(distancebase, level - 1); }

    public String name() { return "Phantom Tunnel"; }
    public String configname() { return "phantom_tunnel"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new PhantomTunnelInstance(id, level, duration(level), caster, target);
    }
}
