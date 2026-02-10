package net.mctitan.rpg.spell.projectile.templates;

import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;

public abstract class DamagingBoltProjectile extends BoltProjectile {
    private DamageRoll damage;

    public DamagingBoltProjectile() {}

    public DamagingBoltProjectile(SpellInstance instance, Location location, Vector velocity) {
        super(instance, location, velocity);
    }

    public DamageRoll damage() { return damage; }
    public void damage(DamageRoll damage) { this.damage = damage; }
}
