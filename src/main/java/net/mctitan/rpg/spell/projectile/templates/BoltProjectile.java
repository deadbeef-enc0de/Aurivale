package net.mctitan.rpg.spell.projectile.templates;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.instance.templates.BoltInstance;
import net.mctitan.rpg.spell.projectile.SpellProjectile;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public abstract class BoltProjectile extends SpellProjectile {
    private static final int BOLT_SECTIONS = 10;

    private Location location;

    public BoltProjectile() {}

    public BoltProjectile(SpellInstance instance, Location location, Vector velocity) {
        super(instance, location, velocity);
    }

    public void sound(Location location) {
        World world = location.world();
        world.playSound(location.bukkit(), sound(), volume(), 1f);
    }

    @Override public Location location() { return location; }

    @Override
    public void remove() {
        // remove projectile
        super.remove();

        // run spell effect
        effect();
    }

    public void create() {
        // set locations
        location = initial();
        lastlocation(initial());
    }

    public void tick(int tick) {
        // set lastlocation
        lastlocation(location());

        // move the projectile small pieces at a time
        World world = location.world();
        for(int section = 0; section <= BOLT_SECTIONS; section++) {
            // set the location of the section
            location = lastlocation().add(velocity().multiply(((double)section)/BOLT_SECTIONS));

            // create particle for effect
            if(particle() == Particle.DUST) {
                world.spawnParticle(particle(), location().bukkit(), 1, 0.1, 0.1, 0.1, 0, new Particle.DustOptions(color(), 1));
            } else {
                world.spawnParticle(particle(), location().bukkit(), 1, 0.1, 0.1, 0.1, 0);
            }

            // check to see if there was a collision with entities bounding box
            for(LivingEntity livingentity : location.bukkit().getNearbyLivingEntities(BoltInstance.SEARCH_RADIUS)) {
                // get entity checked
                Entity entity = DataManager.instance().entity(livingentity);

                // skip missing entity or caster of spell
                if(entity == null || entity == instance().caster()) {
                    continue;
                }

                // be done with this projectile if it hits an entity
                if(entity.bukkitentity().getBoundingBox().contains(location.vector().bukkit())) {
                    remove();
                    return;
                }
            }

            // check to see if there was a collision with a block
            Block block = location.block();
            if(block != null && block.getType().isSolid()) {
                remove();
                return;
            }
        }

        // play the projectile sound
        sound(location);
    }

    public abstract Particle particle();
    public abstract Color color();
    public abstract Sound sound();
    public abstract float volume();
}
