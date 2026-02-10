package net.mctitan.rpg.spell.instance;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.teleporter.Teleporter;
import net.mctitan.rpg.util.teleporter.sequence.AdjacentLocationSequence;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.logging.Level;

public class VoidWalkInstance extends SpellInstance implements Logger {
    public VoidWalkInstance() {}

    public VoidWalkInstance(int id, int level, Entity caster, Entity target) {
        super(id, level, caster, target);
    }

    public SpellType type() { return SpellType.VOID_WALK; }
    public boolean instant() { return true; }

    public void create() {
        // get variables for spell
        Location location = new Location(caster().bukkitentity().getLocation());

        // TODO need to get void dimension when that is ready, does not go back to overworld
        // get world to go to
        World world;
        if(location.world().getName().equals("world")) {
            world = Bukkit.getWorld("world_nether");
        } else {
            world = Bukkit.getWorld("world");
        }

        // make sure the destination world exists
        if(world == null) {
            log(Level.SEVERE, "Void walk disabled, no target dimension exists");
            return;
        }

        // run teleportation
        location.world(world);
        effect(location);
    }

    public void tick(int tick) {}

    public void effect(Location location) {
        // run teleport and don't allow teleporting into water or lava
        Teleporter teleporter = Teleporter.teleport(caster(), new AdjacentLocationSequence(location));
        teleporter.water(false);
        teleporter.lava(false);
    }
}
