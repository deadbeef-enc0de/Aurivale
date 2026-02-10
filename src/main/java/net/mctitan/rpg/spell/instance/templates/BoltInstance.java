package net.mctitan.rpg.spell.instance.templates;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.SpellProjectileType;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.projectile.SpellProjectile;
import net.mctitan.rpg.spell.projectile.templates.BoltProjectile;
import net.mctitan.rpg.spell.type.templates.BoltSpell;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public abstract class BoltInstance extends SpellInstance {
    public static final double SEARCH_RADIUS = 4;

    public BoltInstance() {}

    public BoltInstance(int id, int level, int duration, Entity caster, Entity target) {
        super(id, level, duration, caster, target);
    }

    public boolean instant() { return false; }

    public void create() {
        // get spell object
        BoltSpell spell = (BoltSpell)spell();

        // get default variables for spell

        // apply caster modifications to the spell
        duration(caster().actions().spell().duration().value(duration()));
        double speed = caster().projectiles().speed().value(spell.speed());

        // get the direction of the casting
        Vector direction = null;
        if(target() != null) {
            // from caster eye location to center of bounding box of target
            direction = new Vector(target().bukkitentity().getBoundingBox().getCenter().subtract(caster().bukkitentity().getEyeLocation().toVector()));
        } else {
            direction = new Vector(caster().bukkitentity().getEyeLocation().getDirection());
        }
        Vector velocity = direction.normalize().multiply(speed);

        // get location for start of projectile
        Location location = new Location(caster().bukkitentity().getEyeLocation());

        // get projectile
        BoltProjectile projectile = (BoltProjectile)createprojectile(projectiletype(), location, velocity);

        // create projectile in world
        projectile.create();
    }

    public void tick(int tick) {
        for(SpellProjectile projectile : projectiles()) {
            // tick the projectile in its region scheduler
            Bukkit.getRegionScheduler().run(Aurivale.instance(), projectile.location().bukkit(), task -> {
                projectile.tick(tick);
            });
        }
    }

    public void effect(SpellProjectile projectile) {
        // get the entity that has the bolt in their bounding box
        Entity entity = null;
        for(LivingEntity livingentity : projectile.location().bukkit().getNearbyLivingEntities(SEARCH_RADIUS)) {
            // get the entity
            Entity target = DataManager.instance().entity(livingentity);

            // skip missing entity or caster of spell
            if(target == null || target == caster()) {
                continue;
            }

            // check to see if the location is in the bounding box
            if(target.bukkitentity().getBoundingBox().contains(projectile.location().vector().bukkit())) {
                entity = target;
                break;
            }
        }

        // make sure there is an entity
        if(entity == null) {
            return;
        }

        // call the effect on the entity
        effect(entity, projectile);
    }

    public void effect(Location location) {
        throw new RuntimeException(String.format("%s does not support effect location", spell().name()));
    }

    public abstract SpellProjectileType projectiletype();
    public abstract void effect(Entity entity, SpellProjectile projectile);
}
