package net.mctitan.rpg.spell.instance;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.SpellProjectileType;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.instance.templates.DamagingBoltInstance;

public class ShockBoltInstance extends DamagingBoltInstance {
    public ShockBoltInstance() {}

    public ShockBoltInstance(int id, int level, int duration, Entity caster, Entity target) {
        super(id, level, duration, caster, target);
    }

    public SpellType type() {return SpellType.SHOCK_BOLT; }
    public SpellProjectileType projectiletype() { return SpellProjectileType.SHOCK_BOLT; }
}
