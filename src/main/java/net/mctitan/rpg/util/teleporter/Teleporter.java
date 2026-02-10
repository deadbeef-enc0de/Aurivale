package net.mctitan.rpg.util.teleporter;

import net.mctitan.data.UUID;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import net.mctitan.rpg.util.teleporter.sequence.AdjacentLocationSequence;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class Teleporter {
    private UUID entityid;
    private Vector direction;
    private LocationSequence sequence;
    private boolean water;
    private boolean lava;
    private boolean found;

    private Teleporter(Entity entity, LocationSequence sequence) {
        this.entityid = entity.uuid;
        this.direction = new Vector(entity.bukkitentity().getLocation().getDirection());;
        this.sequence = sequence;

        start();
        schedule();
    }

    public Entity entity() { return DataManager.instance().entity(entityid); }
    public Location current() { return sequence.current(); }

    public void water(boolean water) { this.water = water; }
    public void lava(boolean lava) { this.lava = lava; }

    public static Teleporter teleport(Entity entity, org.bukkit.Location location) { return teleport(entity, new Location(location)); }
    public static Teleporter teleport(Entity entity, Location location) { return teleport(entity, new AdjacentLocationSequence(location)); }
    public static Teleporter teleport(Entity entity, LocationSequence sequence) { return new Teleporter(entity, sequence); }

    private void start() {
        // get location that is 128 blocks above max height of the world
        Location highloc = sequence.current().clone();
        highloc.y(highloc.world().getMaxHeight() + 128);

        // teleport player to high location
        if(entity() != null && entity().bukkitentity() != null) {
            entity().bukkitentity().getScheduler().run(
                    Aurivale.instance(),
                    task -> { entity().bukkitentity().teleportAsync(highloc.bukkit(direction)); },
                    null
            );
        }
    }

    private void schedule() {
        Bukkit.getRegionScheduler().runDelayed(
                Aurivale.instance(),
                sequence.current().bukkit(),
                task -> { check(); },
                5
        );
    }

    private void check() {
        // check entity
        if(entity() == null || entity().bukkitentity() == null) {
            schedule();
            return;
        }

        // get location from sequence
        Location check = sequence.current();

        // if chunk isn't loaded, reschedule for later
        if(!check.isloaded()) {
            start();
            schedule();
            return;
        }

        // search column for a valid location
        for(int y = check.world().getMaxHeight(); y > check.world().getMinHeight(); --y) {
            // set y value
            check.y(y);

            // check to see if player can teleport to that block
            if(check.block().getType() == Material.BEDROCK || check.block().getType().isAir()) {
                continue;
            }

            // check for water/lava and if we are ok with it
            if((!water && check.block().getType() == Material.WATER) ||
               (!lava && check.block().getType() == Material.LAVA)) {
                // stop the search for this column and move to the next one
                break;
            }

            // check blocks above the one found to see if there is space for entity
            found = true; // assume true, we invalidate if not
            int height = (int)Math.ceil(entity().bukkitentity().getHeight());
            for(int h = 1; h <= height; ++h) {
                if(!check.add(0, h, 0).block().getType().isAir()) {
                    found = false;
                    break;
                }
            }

            // if found, break out of column search
            if(found) { break; }
        }

        // if location not found, get the next location and reschedule
        if(!found) {
            sequence.next();
            start();
            schedule();
            return;
        }

        // teleport player to target location
        entity().bukkitentity().setFallDistance(0);
        entity().bukkitentity().teleportAsync(check.add(0, 1, 0).center().bukkit(direction));
    }
}
