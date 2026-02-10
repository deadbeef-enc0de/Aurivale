package net.mctitan.rpg.spell.instance;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.SpellChangedBlock;
import net.mctitan.rpg.spell.type.IcePrisonSpell;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;

public class IcePrisonInstance extends SpellInstance {
    private static final int TARGET_DISTANCE = 64;
    private static final int INITIAL_SOUND_DELAY = 5;

    private int nextsound;

    public IcePrisonInstance() {}

    public IcePrisonInstance(int id, int level, int duration, Entity caster, Entity target) {
        super(id, level, duration, caster, target);
    }

    public SpellType type() { return SpellType.ICE_PRISON; }
    public boolean instant() { return false; }

    public void create() {
        // get spell instance
        IcePrisonSpell spell = (IcePrisonSpell)spell();

        // get some default variables for the spell
        nextsound = INITIAL_SOUND_DELAY;
        Location target;

        // apply caster modifications to the spell
        int radius = (int)caster().actions().spell().area().value(spell.radius(level()));

        // get the block the target is looking at
        // if the target is looking at an entity use its location instead
        org.bukkit.entity.Entity targetentity;
        if(target() != null) {
            targetentity = target().bukkitentity();
        } else {
            targetentity = caster().bukkitentity().getTargetEntity(TARGET_DISTANCE);
        }

        if(targetentity != null && targetentity.isOnGround()) {
            target = new Location(targetentity.getLocation());
        } else {
            Block block = caster().bukkitentity().getTargetBlock(null, TARGET_DISTANCE);
            target = new Location(block.getLocation());
        }

        // make sure there is a usable block at the target location
        if(targetentity == null && target.block().getType().isAir()) {
            duration(0);
            return;
        }
        scheduledloc(target);

        // create change blocks
        for(int dx = -radius; dx <= radius; ++dx) {
            for(int dz = -radius; dz <= radius; ++dz) {
                for(int dy = -radius; dy <= radius; ++dy) {
                    Location location = target.add(dx, dy, dz);
                    double distance = target.bukkit().distance(location.bukkit());
                    if(distance < radius) {
                        SpellChangedBlock block = getblockorcreate(location);
                        block.set(this, Material.ICE);
                    }
                }
            }
        }
    }

    public void tick(int tick) {
        if(tick >= nextsound) {
            // play sound
            World world = scheduledloc().world();
            world.playSound(scheduledloc().bukkit(), Sound.AMBIENT_BASALT_DELTAS_MOOD, 1f, (float)Math.nextDouble(0.5f, 2f));

            // set random delay for next sound
            nextsound += Math.nextInt(20, 30);
        }
    }

    public void effect(Location location) {
        // nothing to do here, this spell is entirely environmental
    }
}
