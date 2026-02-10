package net.mctitan.rpg.spell.instance;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.damage.Damage;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.enums.SpellProjectileType;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.projectile.FireballProjectile;
import net.mctitan.rpg.spell.projectile.SpellProjectile;
import net.mctitan.rpg.spell.type.FireballSpell;
import net.mctitan.rpg.util.directional.DistanceMultiplier;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class FireballInstance extends SpellInstance {
    private static final double BASE_FIREBALL_SPEED = 0.75;
    private static final double MINIMUM_DISTANCE_MULTIPLIER = 0.5;

    private double radius = 0;

    public FireballInstance() {}

    public FireballInstance(int id, int level, int duration, Entity caster, Entity target) {
        super(id, level, duration, caster, target);
    }

    public SpellType type() { return SpellType.FIREBALL; }
    public boolean instant() { return false; }

    public void create() {
        // get spell object
        FireballSpell spell = (FireballSpell)spell();

        // get default variables for spell
        this.radius = spell.radius(level());
        int projectilecount = 1;
        double projectilesangle = Math.PI / 3;

        // apply caster modifications to the spell
        duration(caster().actions().spell().duration().value(duration()));
        radius = caster().actions().spell().area().value(radius);
        projectilecount += 2 * caster().enchantments().level(Enchantment.multishot);
        double speed = caster().projectiles().speed().value(BASE_FIREBALL_SPEED);

        // setup damage object
        Damage damage = caster().actions().spell().damage();
        damage.addflat(spell.damagetype(), spell.mindamage(level()), spell.maxdamage(level()));

        // get the direction of the casting
        Vector direction = null;
        if(target() != null) {
            // from caster eye location to center of bounding box of target
            direction = new Vector(target().bukkitentity().getBoundingBox().getCenter().subtract(caster().bukkitentity().getEyeLocation().toVector()));
        } else {
            direction = new Vector(caster().bukkitentity().getEyeLocation().getDirection());
        }

        // get angle per projectile
        double angleperproj = projectilesangle / projectilecount;

        // create the projectiles
        for(int proj = 0; proj < projectilecount; proj++) {
            // get angle for projectile
            double angle = proj * angleperproj + angleperproj / 2 - projectilesangle / 2;

            // get direction of fireball target
            Vector projdirection = new Vector(direction.bukkit().rotateAroundY(angle));

            // get location and velocity of fireball
            World world = caster().bukkitentity().getWorld();
            Location projlocation = new Location(caster().bukkitentity().getEyeLocation().clone().add(projdirection.bukkit().normalize().multiply(4)));
            Vector velocity = projdirection.normalize().multiply(speed);

            // make fireball object
            FireballProjectile fireball = (FireballProjectile)createprojectile(SpellProjectileType.FIREBALL, projlocation, velocity);

            // roll damage for the fireball
            fireball.damage(damage.roll());

            // tell fireball to create itself
            fireball.create();
        }
    }

    public void tick(int tick) {
        for(SpellProjectile projectile : projectiles()) {
            projectile.projectile().getScheduler().run(Aurivale.instance(), task -> {
                projectile.tick(tick);
            }, null);
        }
    }

    @Override
    public void effect(SpellProjectile projectile) {
        FireballProjectile fireball = (FireballProjectile)projectile;

        // check for player to set last damage
        Player player = null;
        if(caster().bukkitentity().getType() == EntityType.PLAYER) {
            player = (Player)caster();
        }

        for(LivingEntity livingentity : fireball.lastlocation().bukkit().getNearbyLivingEntities(radius)) {
            // get entity hit
            Entity damagee = DataManager.instance().entity(livingentity);
            if(damagee == null) { continue; }

            // get damage and apply distance from center multiplier
            DamageRoll roll = fireball.damage().clone();
            double distance = livingentity.getBoundingBox().getCenter().distance(fireball.lastlocation().bukkit().toVector());
            roll.multiply(DistanceMultiplier.calculate(radius, distance, MINIMUM_DISTANCE_MULTIPLIER));

            // damage target
            damage(damagee, roll);
        }
    }

    public void effect(Location location) {
        throw new RuntimeException(String.format("%s does not support effect location", spell().name()));
    }
}
