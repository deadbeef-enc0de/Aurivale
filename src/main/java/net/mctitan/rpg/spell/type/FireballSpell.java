package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.spell.instance.FireballInstance;
import net.mctitan.rpg.spell.instance.SpellInstance;

public class FireballSpell extends Spell {
    private DamageType damagetype;
    private double mindamageflat;
    private double mindamagebase;
    private double maxdamageflat;
    private double maxdamagebase;

    private double radiusflat;
    private double radiusbase;

    @Override
    public void initialize() {
        super.initialize();

        damagetype = DamageType.valueOf(section().getString("damage.type"));
        mindamageflat = section().getDouble("damage.minimum.flat");
        mindamagebase =  section().getDouble("damage.minimum.base");
        maxdamageflat = section().getDouble("damage.maximum.flat");
        maxdamagebase =  section().getDouble("damage.maximum.base");
        radiusflat = section().getDouble("damage.radius.flat");
        radiusbase =  section().getDouble("damage.radius.base");
    }

    public DamageType damagetype() { return damagetype; }
    public double mindamage(int level) { return mindamageflat * Math.pow(mindamagebase, level - 1); }
    public double maxdamage(int level) { return maxdamageflat * Math.pow(maxdamagebase, level - 1); }
    public double radius(int level) { return radiusflat * Math.pow(radiusbase, level - 1); }

    public String name() { return "Fireball"; }
    public String configname() { return "fireball"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new FireballInstance(id, level, duration(level), caster, target);
    }
}
