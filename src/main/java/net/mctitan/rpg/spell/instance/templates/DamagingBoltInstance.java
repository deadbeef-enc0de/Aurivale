package net.mctitan.rpg.spell.instance.templates;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.damage.Damage;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.spell.projectile.SpellProjectile;
import net.mctitan.rpg.spell.projectile.templates.DamagingBoltProjectile;
import net.mctitan.rpg.spell.type.templates.DamagingBoltSpell;

public abstract class DamagingBoltInstance extends BoltInstance {
    public DamagingBoltInstance() {}

    public DamagingBoltInstance(int id, int level, int duration, Entity caster, Entity target) {
        super(id, level, duration, caster, target);
    }

    @Override
    public void create() {
        // create the projectiles
        super.create();

        // get spell object
        DamagingBoltSpell spell = (DamagingBoltSpell)spell();

        // setup damage object
        Damage damage = caster().actions().spell().damage();
        damage.addflat(spell.damagetype(), spell.mindamage(level()), spell.maxdamage(level()));

        // add the damage to the spell projectiles
        for(SpellProjectile projectile : projectiles()) {
            DamagingBoltProjectile damagebolt =  (DamagingBoltProjectile)projectile;
            damagebolt.damage(damage.roll());
        }
    }

    public void effect(Entity entity, SpellProjectile projectile) {
        // get damaging bolt projectile
        DamagingBoltProjectile damagingbolt = (DamagingBoltProjectile)projectile;

        // get the damage roll
        DamageRoll roll = damagingbolt.damage().clone();

        // damage target
        damage(entity, roll);
    }
}
