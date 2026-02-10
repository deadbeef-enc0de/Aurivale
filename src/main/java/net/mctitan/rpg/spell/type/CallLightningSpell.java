package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.instance.CallLightningInstance;

public class CallLightningSpell extends Spell {
    private double timerflat;
    private double timerbase;

    private double radiusflat;
    private double radiusbase;

    private DamageType damagetype;
    private double mindamageflat;
    private double mindamagebase;
    private double maxdamageflat;
    private double maxdamagebase;

    @Override
    public void initialize() {
        super.initialize();

        timerflat = section().getDouble("timer.flat");
        timerbase = section().getDouble("timer.base");
        radiusflat = section().getDouble("radius.flat");
        radiusbase =  section().getDouble("radius.base");
        damagetype = DamageType.valueOf(section().getString("damage.type"));
        mindamageflat = section().getDouble("damage.minimum.flat");
        mindamagebase =  section().getDouble("damage.minimum.base");
        maxdamageflat = section().getDouble("damage.maximum.flat");
        maxdamagebase =  section().getDouble("damage.maximum.base");
    }

    public int timer(int level) { return (int)Math.round(timerflat * Math.pow(timerbase, level - 1)); }
    public double radius(int level) { return radiusflat * Math.pow(radiusbase, level - 1); }
    public DamageType damagetype() { return damagetype; }
    public double mindamage(int level) { return mindamageflat * Math.pow(mindamagebase, level - 1); }
    public double maxdamage(int level) { return maxdamageflat * Math.pow(maxdamagebase, level - 1); }

    public String name() { return "Call Lightning"; }
    public String configname() { return "call_lightning"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new CallLightningInstance(id, level, duration(level), caster, target);
    }
}
