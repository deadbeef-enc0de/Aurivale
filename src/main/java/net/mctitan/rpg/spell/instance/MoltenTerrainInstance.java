package net.mctitan.rpg.spell.instance;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.SpellChangedBlock;
import net.mctitan.rpg.spell.type.MoltenTerrainSpell;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import net.mctitan.rpg.util.Math;
import org.bukkit.*;
import org.bukkit.block.Block;

public class MoltenTerrainInstance extends SpellInstance {
    private static final int TARGET_DISTANCE = 64;
    private static final int DRIP_HEIGHT = 2;

    private int width;
    private int depth;
    private Location targetloc;

    public MoltenTerrainInstance() {}

    public MoltenTerrainInstance(int id, int level, int duration, Entity caster, Entity target) {
        super(id, level, duration, caster, target);
    }

    public SpellType type() { return SpellType.MOLTEN_TERRAIN; }
    public boolean instant() { return false; }

    public void create() {
        // get spell instance
        MoltenTerrainSpell spell = (MoltenTerrainSpell)spell();

        // get some default variables for the spell
        width = spell.width(level());
        depth = spell.depth(level());

        // apply caster modifications to the spell
        duration(caster().actions().spell().duration().value(duration()));
        width = (int)caster().actions().spell().area().value(width);
        depth = (int)caster().actions().spell().area().value(depth);

        // get the entity or block the caster is looking at
        org.bukkit.entity.Entity targetentity = caster().bukkitentity().getTargetEntity(TARGET_DISTANCE);
        if(targetentity != null && targetentity.isOnGround()) {
            targetloc = new Location(targetentity.getLocation().subtract(0, 1, 0));
        } else {
            Block block = caster().bukkitentity().getTargetBlock(null, TARGET_DISTANCE);
            targetloc = new Location(block.getLocation());
        }

        // if target is null or air, do nothing as we ran out of range
        if(targetloc.block().getType().isAir()) {
            targetloc = null;
            duration(0);
            return;
        }
        scheduledloc(targetloc);

        // get vector and location info
        Vector dv = new Vector(caster().bukkitentity().getLocation().getDirection()).y(0).normalize();
        Vector wv = new Vector(dv.bukkit().rotateAroundY(-Math.PI / 2));
        Location c1 = new Location(targetloc.bukkit().add(wv.bukkit().multiply(-width/2)).add(dv.bukkit().multiply(-depth/2)));

        // go through and find the spell blocks for the spell
        dv = dv.multiply(0.5);
        wv = wv.multiply(0.5);
        for(int w = 0; w < 2 * width; ++w) {
            for(int d = 0; d < 2 * depth; ++d) {
                int v = 0;
                for(int i = 0; Math.abs(v) < 128; ++i, v += i * (i % 2 == 0 ? -1 : 1)) {
                    // get location to check
                    Location location = c1.add(0, v, 0).add(wv.multiply(w)).add(dv.multiply(d)).blockloc();
                    SpellChangedBlock block = getblock(location);

                    // get block type at location
                    Material type = location.block().getType();
                    if(block != null) { type = block.original(); }

                    // get above location
                    Location aboveloc = location.add(0, 1, 0);
                    SpellChangedBlock aboveblock = getblock(aboveloc);

                    // get block type at above location
                    Material abovetype = aboveloc.block().getType();
                    if(aboveblock != null) { abovetype = aboveblock.original(); }

                    // if block isn't solid or above block is solid, keep looking
                    if(!type.isSolid() || abovetype.isSolid()) {
                        continue;
                    }

                    // if the instance already has this block, go to next overhead location
                    if(!hasblock(location)) {
                        getblockorcreate(location);
                    }
                    break;
                }
            }
        }
    }

    public void tick(int tick) {
        if(targetloc == null) {
            return;
        }

        // get world and drop height
        World world = targetloc.world();
        double dripheight = targetloc.y() + DRIP_HEIGHT;

        // go through each spell block on each tick
        for(Location location : changedblocks()) {
            // get the possible block change
            Material current = location.block().getType();
            Material blockchange = Material.VOID_AIR;
            if(current != Material.MAGMA_BLOCK && tick <= 40 && (Math.nextDouble() < tick / 40d)) {
                blockchange = Material.MAGMA_BLOCK;
            } else if(current != Material.LAVA && tick > 40 && tick <= 140 &&
                    (Math.nextDouble() < (tick - 40) / 100d)) {
                blockchange = Material.LAVA;
            } else if(current != Material.MAGMA_BLOCK && tick > (duration() - 220) && tick <= (duration() - 120) &&
                    (Math.nextDouble() < (tick - (duration() - 220)) / 100d)) {
                blockchange = Material.MAGMA_BLOCK;
            }else if(tick > (duration() - 40) && (Math.nextDouble() < (tick - (duration() - 40)) / 40d)) {
                blockchange = null;
            }

            // get the block to change
            SpellChangedBlock block = getblock(location);

            // change or revert the block
            if(blockchange == null) {
                block.revert(this);
            } else if(blockchange != Material.VOID_AIR) {
                block.set(this, blockchange);
            }

            // play extinguish sound effect for magma block on either end
            if(blockchange == Material.MAGMA_BLOCK) {
                world.playSound(location.bukkit(), Sound.BLOCK_FIRE_EXTINGUISH, 0.1f, 0.5f);
            }

            // lava block sends out particles every other second on average
            if(current == Material.LAVA && Math.nextInt(40) == 0 && tick < (duration() - 100)) {
                world.spawnParticle(Particle.LAVA, location.add(0.5, dripheight - location.y(), 0.5).bukkit(), 1, 0.25, 0.25, 0.25, 0);
            }
        }
    }

    public void effect(Location location) {
        // nothing to do here, this spell is entirely environmental
    }
}
