package net.mctitan.rpg.pack;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.pack.EntityPack;
import net.mctitan.rpg.data.tables.weighted.WeightedTable;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.monster.Monster;
import net.mctitan.rpg.monster.Monsters;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.visual.Visuals;
import net.mctitan.rpg.visual.type.EntityShine;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.*;
import java.util.logging.Level;

public class PackManager implements Logger {
    private static final PackManager instance = new PackManager();
    private static final double PACK_SPAWN_DISTANCE = 4;
    private static final int PACK_SPAWN_HEIGHT_SEARCH = 3;

    private WeightedTable<PackQuality> qualities = new WeightedTable<>();
    private Set<Monster> allowed = new HashSet<>();
    private double leadermindistance = 0;
    private double leadermaxdistance = 0;
    private int packsizemin = 0;
    private int packsizemax = 0;

    private int highestqualityweight = 0;

    private PackManager() {
        packdistance();
    }

    public static PackManager instance() { return instance; }
    public static void initialize() { instance.load(); }

    private void load() {
        Configuration packsconfig = Aurivale.instance().getConfig("packs");

        // get the allowed monster types
        for(String monstername : packsconfig.getStringList("monsters")) {
            Monster monster = Monsters.instance().get(monstername);
            if(monster == null) {
                log(Level.WARNING, String.format(""));
                continue;
            }

            allowed.add(monster);
        }

        // load leader distance variables
        leadermindistance = packsconfig.getDouble("distance.minimum");
        leadermaxdistance = packsconfig.getDouble("distance.maximum");

        // get pack size variables
        packsizemin = packsconfig.getInt("pack_size.minimum");
        packsizemax = packsconfig.getInt("pack_size.maximum");

        // load pack qualities
        ConfigurationSection packqualitysection = packsconfig.getConfigurationSection("pack_quality");
        for(String packqualityname : packqualitysection.getKeys(false)) {
            // get pack quality section
            ConfigurationSection qualitysection = packqualitysection.getConfigurationSection(packqualityname);

            // verify something is good
            if(!qualitysection.contains("weight") || !qualitysection.contains("groups") || !qualitysection.contains("modifiers")) {
                log(Level.WARNING, String.format("Cannot load pack quality=%s invalid", packqualityname));
            }

            // creat the pack quality and add to table
            int weight = qualitysection.getInt("weight");
            int groups = qualitysection.getInt("groups");
            int modifiers = qualitysection.getInt("modifiers");

            // setup visual color
            Color color = null;
            if(qualitysection.contains("color")) {
                int red = qualitysection.getInt("color.red");
                int green = qualitysection.getInt("color.green");
                int blue = qualitysection.getInt("color.blue");
                color = Color.fromRGB(red, green, blue);
            }

            // get any modifiers added from quality
            List<Modifier> extramods = new LinkedList<>();
            if(qualitysection.contains("qualitymods")) {
                ConfigurationSection qualitymodsection = qualitysection.getConfigurationSection("qualitymods");
                for(String modifiername :  qualitymodsection.getKeys(false)) {
                    ConfigurationSection modesction = qualitymodsection.getConfigurationSection(modifiername);
                    ModifierTemplate template = ModifierTemplate.template(modesction);
                    if(template != null) {
                        extramods.add(template.modifier());
                    }
                }
            }

            // get highest pack quality weight
            if(weight > highestqualityweight) { highestqualityweight = weight; }

            PackQuality packquality = new PackQuality(packqualityname, groups, modifiers, color, extramods);
            qualities.insert(packquality, weight);
            log(Level.INFO, String.format("Added pack quality=%s", packqualityname));
        }
    }

    public boolean allowed(EntityType type) { return allowed(Monsters.instance().get(type)); }
    public boolean allowed(Monster monster) { return allowed.contains(monster); }
    public PackQuality quality(int luck) { return qualities.get(Math.max(luck, 1 - highestqualityweight)); }

    public void create(Entity leader) { create(leader, false); }
    public void create(Entity leader, boolean force) {
        // make sure the leader has an allowed monster type
        if(!force && !allowed.contains(leader.monster())) {
            return;
        }

        // create entity pack
        EntityPack pack = DataManager.instance().pack(leader);

        // get pack size
        int packsize = Math.nextInt(packsizemin, packsizemax);

        // get center location
        World world = leader.bukkitentity().getWorld();
        Location center = new Location(leader.bukkitentity().getBoundingBox().getCenter().toLocation(world));

        // spawn followers and add them to the pack
        List<Location> locations = nearbylocs(center, packsize - 1);
        for(Location location : locations) {
            LivingEntity bukkit = (LivingEntity)world.spawnEntity(location.bukkit(), leader.bukkitentity().getType(), SpawnReason.CUSTOM);
            Entity follower = DataManager.instance().entity(bukkit);
            pack.add(follower);
        }
        log(Level.INFO, String.format("Created pack for type=%s size=%d", leader.bukkitentity().getType(), pack.followers().size() + 1));

        // get modifiers
        PackQuality quality = quality(leader.dropluck().killerluck());
        for(Modifier modifier : PackModifiers.instance().modifiers(quality.groups(), quality.modifiers())) {
            log(Level.INFO, String.format("  Adding modifier=%s rank=%d to pack", modifier.id(), modifier.rank()));
            leader.apply(modifier);
            for(Entity follower : pack.followers()) {
                follower.apply(modifier);
            }
        }

        // add particle effect to pack quality that has configured color
        if(quality.color() != null) {
            Visuals.instance().add(new EntityShine(leader, quality.color()));
            for(Entity follower : pack.followers()) {
                Visuals.instance().add(new EntityShine(follower, quality.color()));
            }
        }

        // add quality mods
        for(Modifier modifier : quality.extramods()) {
            log(Level.INFO, String.format("  Adding modifier=%s rank=%d to pack", modifier.id(), modifier.rank()));
            leader.apply(modifier);
            for(Entity follower : pack.followers()) {
                follower.apply(modifier);
            }
        }

        // save the updated entity pack and all entities
        DataManager.instance().save(pack);
        DataManager.instance().save(leader);
        for(Entity follower : pack.followers()) {
            DataManager.instance().save(follower);
        }
    }

    private List<Location> nearbylocs(Location location, int count) {
        List<Location> ret =  new LinkedList<>();
        int tries = 0;

        while(ret.size() < count && tries < 100) {
            // increment tries
            ++tries;

            // get initial location
            double angle = 2 * Math.PI * Math.nextDouble();
            double dx = PACK_SPAWN_DISTANCE * Math.cos(angle);
            double dz = PACK_SPAWN_DISTANCE * Math.sin(angle);
            Location randloc = location.add(dx, 0, dz);

            // get height starting at 0 and working out from there
            Set<Integer> heights = new HashSet<>();
            for(int d = 0; d <= PACK_SPAWN_HEIGHT_SEARCH; ++d) {
                for(int dy = -d; dy <= d; dy += 2*d) {
                    // to stop the double 0 check
                    if(heights.contains(dy)) { break; }
                    heights.add(dy);

                    // get test location
                    Location testloc = randloc.add(0, dy, 0);

                    // get nearby types
                    Material belowtype = testloc.add(0, -1, 0).block().getType();
                    Material type = testloc.block().getType();
                    Material abovetype = testloc.add(0, 1, 0).block().getType();

                    // make sure location underneath is a solid block
                    if(!belowtype.isSolid() && belowtype != Material.WATER && belowtype != Material.LAVA) {
                        continue;
                    }

                    // make sure block and one above are air
                    if((type.isAir() || type == Material.WATER || type == Material.LAVA) &&
                       (abovetype.isAir() || abovetype == Material.WATER || abovetype == Material.LAVA)) {
                        ret.add(testloc);
                        break;
                    }
                }
            }
        }

        return ret;
    }

    private void packdistance() {
        // every tick set target for checked entities
        HashSet<Entity> checkset = new HashSet<>();
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(Aurivale.instance(), task -> {
            // make copy of entities to check
            HashSet<Entity> entities = new HashSet<>();
            synchronized (checkset) { entities.addAll(checkset); }

            // go through each entity and set leader as target
            for(Entity entity : entities) {
                // make sure bukkit entity exists
                if(entity.bukkitentity() == null) { continue; }

                // run the code in the entities thread
                entity.bukkitentity().getScheduler().run(Aurivale.instance(), entitytask -> {
                    // make sure entity is regrouping, has a pack, and has a leader and if not remove from checkset
                    if(!entity.regouping() || entity.pack() == null || entity.pack().leader() == null) {
                        // set regrouping to false just in case
                        entity.regrouping(false);

                        // stop path finding
                        entity.mob().getPathfinder().stopPathfinding();

                        // remove entity from set of entities to check and return
                        synchronized (checkset) { checkset.remove(entity); }
                        return;
                    }

                    // path find entity to leader
                    entity.mob().getPathfinder().moveTo(entity.pack().leader().bukkitentity());
                }, null);
            }
        }, 1, 1);

        // every second find entities that need to move back to their leader
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(Aurivale.instance(), task -> {
            Set<EntityPack> packs = DataManager.instance().packs();
            if(packs.isEmpty()) { return; }

            // get some performance statistics
            long time = System.nanoTime();
            int followers = 0;

            // entities to add to check list
            Set<Entity> checkadds = new HashSet<>();

            // go through each pack and check follower distance
            for(EntityPack pack : packs) {
                // get pack leader
                Entity leader = pack.leader();
                if(leader == null || leader.bukkitentity() == null) {
                    // unload pack and move on
                    DataManager.instance().unload(pack);
                    continue;
                }
                followers += pack.followers().size();

                // run the check with followers in the thread that owns the leader
                leader.bukkitentity().getScheduler().run(Aurivale.instance(), leadertask -> {
                    // get leader location
                    World world = leader.bukkitentity().getWorld();
                    Location leaderloc = new Location(leader.bukkitentity().getBoundingBox().getCenter().toLocation(world));

                    // go through each follower and check distance to leader
                    for(Entity follower : pack.followers()) {
                        // make sure follow is online, correct type, and in the same world
                        if(follower.bukkitentity() == null || follower.mob() != null && follower.bukkitentity().getWorld() != world) {
                            if(follower.mob() != null) {
                                // follower is now in another world, need to stop following leader
                                follower.regrouping(false);
                            }
                            continue;
                        }

                        // get distance of follower from leader
                        Location followerloc =  new Location(follower.bukkitentity().getBoundingBox().getCenter().toLocation(world));
                        double distance = followerloc.fastdistance(leaderloc);

                        // if the follower is too far away and doesn't have a target, move to leader
                        if(distance > leadermaxdistance && !follower.regouping()) {
                            // entity regrouping, send to leader
                            follower.regrouping(true);
                            checkadds.add(follower);
                        }

                        // follower is too close and is a pathing to leader, stop movement
                        else if(distance < leadermindistance && follower.regouping()) {
                            // entity done regrouping stop path finding
                            follower.regrouping(false);
                        }
                    }
                }, null);
            }

            // print out performance statistics
            log(Level.INFO, String.format("Processed %d followers for %d packs in %.03fms",
                    followers,
                    packs.size(),
                    (System.nanoTime() - time) / 1000000d
            ));

            // if there are entities to add to check list, do that
            if(!checkadds.isEmpty()) {
                synchronized (checkset) { checkset.addAll(checkadds); }
            }
        }, 20, 20);
    }
}
