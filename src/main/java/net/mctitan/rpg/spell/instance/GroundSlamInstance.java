package net.mctitan.rpg.spell.instance;

import net.mctitan.data.UUID;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.damage.Damage;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.SpellChangedBlock;
import net.mctitan.rpg.spell.type.GroundSlamSpell;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;

public class GroundSlamInstance extends SpellInstance {
    private static final int STEPS = 24;
    private static final double DISTANCE_INCREMENT = 0.25;
    private static double ONE_THIRD = 1.0 / 3;
    private static double TWO_THIRD = 2.0 / 3;

    private Damage damage;
    private double distance = 0;
    private Location origin;
    private HashMap<UUID,String> distances = new HashMap<>();

    public GroundSlamInstance() {}

    public GroundSlamInstance(int id, int level, int duration, Entity caster, Entity target) {
        super(id, level, duration, caster, target);
    }

    public SpellType type() { return SpellType.GROUND_SLAM; }
    public boolean instant() { return false; }

    public void create() {
        // get spell instance
        GroundSlamSpell spell = (GroundSlamSpell)spell();

        // get some default variables for the spell
        distance = spell.distance(level());
        double sweep = Math.PI / 3;

        // apply caster modifications to the spell
        duration(caster().actions().spell().duration().value(duration()));
        distance = caster().actions().spell().area().value(distance);
        sweep *= Math.sqrt(caster().actions().spell().area().scaling());

        // setup damage object
        damage = caster().actions().spell().damage();
        damage.addflat(spell.damagetype(), spell.mindamage(level()), spell.maxdamage(level()));

        // get the direction of the casting
        Vector direction;
        if(target() != null) {
            direction = new Vector(target().bukkitentity().getBoundingBox().getCenter().subtract(caster().bukkitentity().getEyeLocation().toVector()));
        } else {
            direction = new Vector(caster().bukkitentity().getLocation().getDirection());
        }
        direction = direction.y(0).normalize();
        Vector start = new Vector(direction.bukkit().rotateAroundY(-sweep / 2));

        // get the start block
        origin = new Location(caster().bukkitentity().getEyeLocation().add(0,-2,0).add(direction.bukkit().multiply(1.5)));
        scheduledloc(origin);

        // create all SpellChangedBlock objects
        double increment = sweep / STEPS;
        for(int step = 0; step < STEPS; step++) {
            Vector angle = new Vector(start.bukkit().rotateAroundY(increment * step));
            for(double d = 0; d <= distance; d += DISTANCE_INCREMENT) {
                Vector dangle = angle.normalize().multiply(d);
                Location location = origin.add(dangle);

                // make sure the spell instance doesn't already have a block here
                if(hasblock(location)) { continue; }

                // check to see if we should move up or down a block
                if(!location.block().getType().isSolid()) {
                    location = location.add(0, -1, 0);
                } else if(location.add(0, 1, 0).block().getType().isSolid()) {
                    location = location.add(0, 1, 0);
                }

                // make sure we still have a good block
                if(!location.block().getType().isSolid() ||
                   location.add(0, 1, 0).block().getType().isSolid()) {
                    continue;
                }

                // create the block object
                SpellChangedBlock block = getblockorcreate(location);

                // get the distance/height for the block
                String slamdistance = d / distance < ONE_THIRD ? GroundSlamSpell.NEAR :
                                      d / distance < TWO_THIRD ? GroundSlamSpell.CLOSE :
                                                                 GroundSlamSpell.FAR;
                distances.put(block.uuid, slamdistance);
            }
        }
    }

    private void sound(Location location) {
        World world = location.world();
        world.playSound(location.bukkit(), Sound.BLOCK_BASALT_BREAK ,0.5f, 1f);
    }

    public void tick(int tick) {
        // these are holds for the switch statements
        SpellChangedBlock stalagmiteblock = null;
        PointedDripstone blockdata = null;
        Location stalagmiteloc = null;

        // loop through all blocks and handle their state
        for(Location location : this.changedblocks()) {
            // get distance from origin
            double distance = origin.bukkit().distance(location.bukkit());

            // calculate block tick count
            int blocktick = (int)(tick - 2 * distance);
            if(blocktick < 0) { continue; } // invalid time

            // get distance/height for block
            SpellChangedBlock block = getblock(location);
            if(block == null) { continue; } // stalagmite block is reverted
            String slam = distances.get(block.uuid);
            if(slam == null) { continue; } // slam being null means stalagmite block

            // do things based on the block tick
            switch(blocktick) {
                case 2 -> { block.set(this, Material.DRIPSTONE_BLOCK); }
                case 4, 6, 8 -> {
                    stalagmiteloc = location.clone();
                    switch(slam) {
                        case GroundSlamSpell.NEAR:
                            if(blocktick >= 8) {
                                stalagmiteloc = stalagmiteloc.add(0, 1, 0);
                                stalagmiteblock = getblockorcreate(stalagmiteloc);
                                stalagmiteblock.set(this, Material.POINTED_DRIPSTONE);
                                blockdata = stalagmiteblock.blockdata(this);
                                blockdata.setThickness(PointedDripstone.Thickness.BASE);
                                stalagmiteblock.blockdata(this, blockdata);
                                sound(stalagmiteloc);
                            }
                        case GroundSlamSpell.CLOSE:
                            if(blocktick >= 6) {
                                stalagmiteloc = stalagmiteloc.add(0, 1, 0);
                                stalagmiteblock = getblockorcreate(stalagmiteloc);
                                stalagmiteblock.set(this, Material.POINTED_DRIPSTONE);
                                blockdata = stalagmiteblock.blockdata(this);
                                blockdata.setThickness(PointedDripstone.Thickness.FRUSTUM);
                                stalagmiteblock.blockdata(this, blockdata);
                                sound(stalagmiteloc);
                            }
                        case GroundSlamSpell.FAR:
                            stalagmiteloc = stalagmiteloc.add(0, 1, 0);
                            stalagmiteblock = getblockorcreate(stalagmiteloc);
                            stalagmiteblock.set(this, Material.POINTED_DRIPSTONE);
                            blockdata = stalagmiteblock.blockdata(this);
                            blockdata.setThickness(PointedDripstone.Thickness.TIP);
                            stalagmiteblock.blockdata(this, blockdata);
                            sound(stalagmiteloc);
                    }
                }
                case 10 -> { effect(location); }
                case 22 -> {
                    block.revert(this);
                    stalagmiteloc = location.clone();
                    switch(slam) {
                        case GroundSlamSpell.NEAR:
                            stalagmiteloc = stalagmiteloc.add(0, 1, 0);
                            getblock(stalagmiteloc).revert(this);
                        case GroundSlamSpell.CLOSE:
                            stalagmiteloc = stalagmiteloc.add(0, 1, 0);
                            getblock(stalagmiteloc).revert(this);
                        case GroundSlamSpell.FAR:
                            stalagmiteloc = stalagmiteloc.add(0, 1, 0);
                            getblock(stalagmiteloc).revert(this);
                    }
                }
            }
        }
    }

    public void effect(Location location) {
        // get spell
        GroundSlamSpell spell =  (GroundSlamSpell)spell();

        // get block and slam data
        SpellChangedBlock block = getblock(location);
        String slam = distances.get(block.uuid);
        double halfheight = (spell.height(slam) + 1) / 2d;

        // get bounding box of this part of the slam
        BoundingBox slambox = new BoundingBox(
            location.blockx(), location.blocky() + 1, location.blockz(),
            location.blockx() + 1, location.blocky() + spell.height(slam) + 1, location.blockz() + 1
        );

        // if caster is player, get that
        Player player = null;
        if(caster().bukkitentity().getType() == EntityType.PLAYER) {
            player = (Player)caster();
        }

        // get list of possible targets
        Location target = location.center().y(location.blocky()).add(0, halfheight, 0);
        for (LivingEntity livingentity : target.bukkit().getNearbyLivingEntities(0.5, halfheight)) {
            // get entity object
            Entity damagee = DataManager.instance().entity(livingentity);

            // make sure the entity still exists and isn't the caster
            if(damagee == null || damagee == caster()) {
                continue;
            }

            // check to see stalagmite hits
            if(damagee.bukkitentity().getLocation().getBlockX() != location.blockx() ||
                    damagee.bukkitentity().getLocation().getBlockZ() != location.blockz() ||
                    !damagee.bukkitentity().getBoundingBox().overlaps(slambox)) {
                continue;
            }

            // get damage
            DamageRoll roll = damage.roll();
            roll.multiply(spell.damagemulti(slam));

            // damage target
            damage(damagee, roll);

            //knockback entity
            Location diff = new Location(livingentity.getLocation().subtract(origin.bukkit()));
            Vector direction = diff.vector().y(0).normalize();
            damagee.bukkitentity().teleportAsync(damagee.bukkitentity().getLocation().add(0,spell.height(slam) + 1,0));
            damagee.bukkitentity().setVelocity(direction.multiply(spell.height(slam)).bukkit());
        }
    }
}
