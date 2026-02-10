package net.mctitan.rpg.spell.instance;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.SpellChangedBlock;
import net.mctitan.rpg.spell.type.PhantomTunnelSpell;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;

import java.util.List;

public class PhantomTunnelInstance extends SpellInstance {
    private static final double DISTANCE_STEP = 0.25;
    private static final int SOUND_TIMER = 40;
    private static final double PARTICLE_CHANCE = 0.05;

    public PhantomTunnelInstance() {}

    public PhantomTunnelInstance(int id, int level, int duration, Entity caster, Entity target) {
        super(id, level, duration, caster, target);
    }

    public SpellType type() { return SpellType.PHANTOM_TUNNEL; }
    public boolean instant() { return false; }

    public void create() {
        // get spell instance
        PhantomTunnelSpell spell = (PhantomTunnelSpell)spell();

        // get some default variables for the spell
        double distance = spell.distance(level());

        // apply caster modifications to the spell
        duration(caster().actions().spell().duration().value(duration()));
        distance = caster().actions().spell().area().value(distance);

        // get the direction of the casting
        Vector direction;
        if(target() != null) {
            direction = new Vector(target().bukkitentity().getBoundingBox().getCenter().subtract(caster().bukkitentity().getEyeLocation().toVector()));
        } else {
            direction = new Vector(caster().bukkitentity().getLocation().getDirection());
        }
        direction = direction.normalize();

        // go through and add blocks to be changed
        Location eyeloc = new Location(caster().bukkitentity().getEyeLocation());
        for(double d = 0; d <= distance; d += DISTANCE_STEP) {
            Location center = eyeloc.add(direction.multiply(1.5 + d));

            // change 3x3 cube surrounding block
            for(int dx = -1; dx <= 1; ++dx) {
                for(int dz = -1; dz <= 1; ++dz) {
                    for(int dy = -1; dy <= 1; ++dy) {
                        Location blockloc = center.add(dx, dy, dz);
                        if(blockloc.block().getType() == Material.BEDROCK || hasblock(blockloc)) {
                            continue;
                        }

                        SpellChangedBlock block = getblockorcreate(blockloc);
                        block.set(this, Material.AIR);
                    }
                }
            }
        }
    }

    public void tick(int tick) {
        // get changed blocks locations
        List<Location> blocks = changedblocks().stream().toList();

        // play sound from a random block ever other second
        if((tick % SOUND_TIMER) == 0) {
            Location location = blocks.get(Math.nextInt(blocks.size()));
            location.world().playSound(location.bukkit(), Sound.PARTICLE_SOUL_ESCAPE, 1f, 1f);
        }

        // create particle effects
        for(Location location : blocks) {
            // make sure the original block wasn't air
            SpellChangedBlock block = getblock(location);
            if(block == null || block.original() == null || block.original().isAir()) {
                continue;
            }

            // see if we should make a particle
            if(Math.nextDouble() < PARTICLE_CHANCE) {
                World world = location.world();
                world.spawnParticle(Particle.PORTAL, location.bukkit(), 1, 0.5, 0.5, 0.5, 0);
            }
        }
    }

    public void effect(Location location) {
        // nothing to do as there is no effect
    }
}
