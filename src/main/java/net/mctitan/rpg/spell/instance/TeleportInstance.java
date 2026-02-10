package net.mctitan.rpg.spell.instance;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.type.TeleportSpell;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;

public class TeleportInstance extends SpellInstance {
    private static final double DISTANCE_STEP = 0.2;

    public TeleportInstance() {}

    public TeleportInstance(int id, int level, Entity caster, Entity target) {
        super(id, level, caster, target);
    }

    public SpellType type() { return SpellType.TELEPORT; }
    public boolean instant() { return true; }

    public void create() {
        // get spell instance
        TeleportSpell spell = (TeleportSpell)spell();

        // get some default variables for the spell
        int distance = spell.distance(level());

        // apply caster modifications to the spell
        distance = (int)caster().actions().spell().area().value(distance);

        // get the direction of the casting
        Location location = new Location(caster().bukkitentity().getEyeLocation());
        Vector direction = new Vector(caster().bukkitentity().getEyeLocation().getDirection());
        direction = direction.normalize().multiply(DISTANCE_STEP);

        // find target block
        Location previous;
        Location target = location;
        int d = 0;
        while(d < distance) {
            previous = target;
            do {
                target = target.add(direction);
            } while(target.blockequals(previous));

            if(!target.block().getType().isAir()) {
                break;
            }

            ++d;
        }

        // target not found, failure
        if(target.block().getType().isAir()) {
            return;
        }

        // find good spot to land
        if(!target.add(0, 1,0).block().getType().isAir() ||
           !target.add(0, 2, 0).block().getType().isAir()) {
            // blocks above are obstructed, come back 1 block against the wall
            previous = target;
            do {
                target = target.add(direction.multiply(-1));
            } while(target.blockequals(previous));
        }

        // run the teleportation effect
        effect(target.center().add(0, 1, 0));
    }

    public void tick(int tick) {}

    public void effect(Location location) {
        // get some data
        Location entity = new Location(caster().bukkitentity().getLocation());
        Vector direction = new Vector(caster().bukkitentity().getEyeLocation().getDirection().normalize());
        double particlesdistance = entity.bukkit().distance(location.bukkit());
        World world = entity.world();

        // create particle effect between locations
        for(double d = DISTANCE_STEP; d < particlesdistance; d += DISTANCE_STEP) {
            Location p = entity.add(direction.multiply(d));
            world.spawnParticle(Particle.CRIT, p.bukkit(), 3, 0.25, 0.25, 0.25, 0);
        }

        // play sound effect for source and destination
        world.playSound(entity.bukkit(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1f, 1f);
        world.playSound(location.bukkit(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1f, 1f);

        // teleport player
        org.bukkit.Location l = location.bukkit();
        l.setDirection(direction.bukkit());
        caster().bukkitentity().teleportAsync(l);
    }
}
