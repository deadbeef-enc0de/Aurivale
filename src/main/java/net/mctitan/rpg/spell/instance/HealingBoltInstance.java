package net.mctitan.rpg.spell.instance;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.SpellProjectileType;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.instance.templates.BoltInstance;
import net.mctitan.rpg.spell.projectile.HealingBoltProjectile;
import net.mctitan.rpg.spell.projectile.SpellProjectile;
import net.mctitan.rpg.spell.type.HealingBoltSpell;
import net.mctitan.rpg.util.Logger;

import java.util.logging.Level;

public class HealingBoltInstance extends BoltInstance implements Logger {
    public HealingBoltInstance() {}

    public HealingBoltInstance(int id, int level, int duration, Entity caster, Entity target) {
        super(id, level, duration, caster, target);
    }

    @Override
    public void create() {
        // create the projectiles
        super.create();

        // get spell object
        HealingBoltSpell spell = (HealingBoltSpell)spell();

        // add healing to projectile
        for(SpellProjectile projectile : projectiles()) {
            HealingBoltProjectile healingbolt =  (HealingBoltProjectile)projectile;
            healingbolt.healing(spell.healing(level()));
        }
    }

    public SpellType type() {return SpellType.HEALING_BOLT; }
    public SpellProjectileType projectiletype() { return SpellProjectileType.HEALING_BOLT; }

    public void effect(Entity entity, SpellProjectile projectile) {
        // get healing bolt projectile
        HealingBoltProjectile healingbolt = (HealingBoltProjectile)projectile;

        // heal the entity
        entity.heal(healingbolt.healing());
        log(Level.INFO, String.format("%s's %s-%d healing %.02f health to %s",
                caster().name(),
                type().spell().name(),
                level(),
                healingbolt.healing(),
                entity.name()
        ));
    }
}
