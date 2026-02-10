package net.mctitan.rpg.spell.projectile;

import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.enums.SpellProjectileType;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;

public class FireballProjectile extends SpellProjectile {
    private DamageRoll damage;

    public FireballProjectile() {}

    public FireballProjectile(SpellInstance instance, Location location, Vector velocity) {
        super(instance, location, velocity);
    }

    public DamageRoll damage() { return damage; }
    public void damage(DamageRoll damage) { this.damage = damage; }

    @Override
    public void remove() {
        // remove projectile
        super.remove();

        // create explosion visual effect
        World world = lastlocation().world();
        world.playSound(lastlocation().bukkit(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 0f);
        world.spawnParticle(Particle.EXPLOSION, lastlocation().bukkit(), 1);
        world.spawnParticle(Particle.SMOKE, lastlocation().bukkit(), 300, 2, 2, 2, 0);
        world.spawnParticle(Particle.FLAME, lastlocation().bukkit(), 300, 2, 2, 2, 0);

        // run the spell instance effect
        effect();
    }

    public void create() {
        // set last location in case projectile immediately disappears
        lastlocation(initial());

        // create fireball entity
        World world =  lastlocation().world();
        Fireball fireball = (Fireball)world.spawnEntity(lastlocation().bukkit(), EntityType.FIREBALL);
        projectile(fireball);

        // set some attributes
        fireball.setIsIncendiary(false);
        fireball.setPersistent(true);
        fireball.setYield(0f);

        // set velocity and direction
        fireball.setVelocity(velocity().bukkit());
        fireball.setDirection(velocity().normalize().bukkit());
    }

    public void tick(int tick) {
        // make sure the projectile exists
        if(projectile() == null) { return; }

        // set the last location to the current projectile location
        lastlocation(location());

        // play sound effect for the fireball
        World world = projectile().getWorld();
        if((tick & 0x7) == 0) {
            world.playSound(location().bukkit(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 1f, 1f);
        }

        // create trailing smoke effect
        world.spawnParticle(Particle.SMOKE, location().bukkit(), 20, 0.25, 0.25, 0.25, 0);
        world.spawnParticle(Particle.FLAME, location().bukkit(), 3, 0.25, 0.25, 0.25, 0);

        // maintain the speed of the projectile
        projectile().setVelocity(velocity().bukkit());
    }

    public SpellProjectileType type() { return SpellProjectileType.FIREBALL; }
}
