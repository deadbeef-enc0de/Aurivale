package net.mctitan.rpg.handler.general;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.pack.EntityPack;
import net.mctitan.rpg.monster.Monster;
import net.mctitan.rpg.monster.Monsters;
import net.mctitan.rpg.pack.PackManager;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.world.ChunkPopulateEvent;

public class EntityPacks implements Listener {
    private static final EntityPacks instance = new EntityPacks();

    private final double packsearchdistance;
    private final int packcount;
    private final double packsearchhorizontal;
    private final double packsearchvertical;
    private final double playersearchhorizontal;
    private final double playersearchvertical;

    private EntityPacks() {
        packsearchdistance = Aurivale.instance().getConfig("packs").getDouble("spawn.local_pack_search");
        packcount = Aurivale.instance().getConfig("packs").getInt("spawn.count");
        packsearchhorizontal = Aurivale.instance().getConfig("packs").getDouble("spawn.search.horizontal");
        packsearchvertical = Aurivale.instance().getConfig("packs").getDouble("spawn.search.vertical");
        playersearchhorizontal = Aurivale.instance().getConfig("packs").getDouble("spawn.distance.horizontal");
        playersearchvertical = Aurivale.instance().getConfig("packs").getDouble("spawn.distance.vertical");
    }

    public static EntityPacks instance() { return instance; }

    @EventHandler
    public void onEntityAdd(EntityAddToWorldEvent event) {
        // make sure entity type added is one that can be a pack
        if(!(event.getEntity() instanceof LivingEntity) || !PackManager.instance().allowed(event.getEntityType())) {
            return;
        }

        // get entity
        Entity entity = DataManager.instance().entity(event.getEntity().getUniqueId());
        if(entity == null) {
            return;
        }

        // get entity pack to load it
        EntityPack pack = entity.pack();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // get the entity that died
        Entity entity = DataManager.instance().entity(event.getEntity().getUniqueId());

        // check to see if the entity was the leader of a pack
        if(entity == null || entity.pack() == null || entity.pack().leader() != entity) {
            // do nothing
            return;
        }

        // fully remove the entity pack
        DataManager.instance().remove(entity.pack().uuid);
    }

    @EventHandler
    public void onEntityPathfind(EntityPathfindEvent event) {
        // get the entities
        Entity entity = DataManager.instance().entity(event.getEntity().getUniqueId());
        Entity target = event.getTargetEntity() != null ? DataManager.instance().entity(event.getTargetEntity().getUniqueId()) : null;
        if(entity == null) { return; }

        // get the pack
        EntityPack pack = entity.pack();
        if(pack == null || entity == pack.leader()) { return; }

        // if new target is leader, allow it no matter what
        if(target == pack.leader()) { return; }

        // cancel event if entity ie regrouping
        event.setCancelled(entity.regouping());
    }

    @EventHandler(priority =  EventPriority.LOWEST)
    public void onEntityTarget(EntityTargetEvent event) {
        // if event cancelled or not a living entity target, do nothing
        if(event.isCancelled()) { return; }

        // get the entity
        Entity entity =  DataManager.instance().entity(event.getEntity().getUniqueId());
        if(entity == null) { return; }

        // get the pack
        EntityPack pack = entity.pack();
        if(pack == null || entity == pack.leader()) { return ; }

        // cancel event if entity is regrouping if this was not an attack
        if(event.getReason() != EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY) {
            event.setCancelled(entity.regouping());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityTargetWatch(EntityTargetEvent event) {
        // if event cancelled or not a living entity target, do nothing
        if(event.isCancelled() || !(event.getTarget() instanceof LivingEntity bukkitentity)) { return; }

        // get entity
        Entity entity = DataManager.instance().entity(event.getEntity().getUniqueId());
        if(entity == null) { return; }

        // event is for propagating targets to hostile mobs
        if(entity.monster().passive()) { return; }

        // get entity pack
        EntityPack pack = entity.pack();
        if(pack == null) { return; }

        // if the pack has no leader, bail
        if(pack.leader() == null) { return; }

        // entity is leader or leader does not have target, set target
        if(pack.leader() == entity || pack.leader().mob().getTarget() == null) {
            pack.leader().mob().setTarget(bukkitentity);
            for(Entity follower : pack.followers()) {
                follower.regrouping(false);
                follower.mob().setTarget(bukkitentity);
            }
        }
    }

    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {
        for(org.bukkit.entity.Entity entity : event.getChunk().getEntities()) {
            // see if the entity is a living entity and can make a pack
            if(!(entity instanceof LivingEntity livingentity) || !PackManager.instance().allowed(livingentity.getType())) {
                continue;
            }

            // try to make a pack and remove entity on failure
            if(!createpack(livingentity)) {
                livingentity.remove();
            }
        }
    }

    @EventHandler
    public void onCreatureBreed(EntityBreedEvent event) {
        Entity mother = DataManager.instance().entity(event.getMother());
        Entity father = DataManager.instance().entity(event.getFather());
        Entity child = DataManager.instance().entity(event.getEntity());

        // at least one parent has a pack
        if(mother.pack() != null || father.pack() != null) {
            // get pack, prefer mother's pack
            EntityPack pack = (mother.pack() != null ? mother.pack() : father.pack());

            // add child to pack and save
            pack.add(child);
            DataManager.instance().save(pack);
            DataManager.instance().save(child);
        }

        // create a new pack with mother, father, and child
        else {
            // create new pack with mother as leader
            EntityPack pack = DataManager.instance().pack(mother);

            // add father and child
            pack.add(father);
            pack.add(child);

            // save objects
            DataManager.instance().save(pack);
            DataManager.instance().save(mother);
            DataManager.instance().save(father);
            DataManager.instance().save(child);
        }
    }

    @EventHandler
    public void onCreateSpawn(CreatureSpawnEvent event) {
        // make sure spawn reason is natural and type is allowed to be a pack
        if(event.getSpawnReason() != SpawnReason.NATURAL || !PackManager.instance().allowed(event.getEntityType())) {
            // we only bail because we want the entity to be created still
            return;
        }

        // try to create the pack, cancel if there is a failure
        event.setCancelled(!createpack(event.getEntity()));
    }

    private boolean createpack(LivingEntity livingentity) {
        // get spawn location
        Location location = livingentity.getLocation();

        // make sure there isn't an entity pack of the same entity type nearby
        for(LivingEntity bukkitentity : location.getNearbyLivingEntities(packsearchdistance)) {
            // if it's a different entity type, skip
            if(bukkitentity.getType() != livingentity.getType()) {
                continue;
            }

            Entity local = DataManager.instance().entity(bukkitentity);
            if(local == null || local.pack() != null) {
                // return failure
                return false;
            }
        }

        // make sure there is a player within the distance of the pack unless it's passive
        Monster monster = Monsters.instance().get(livingentity.getType());
        int players = location.getNearbyEntitiesByType(Player.class, playersearchhorizontal, playersearchvertical).size();
        if(!monster.passive() && players < 1) {
            // return failure
            return false;
        }

        // get count of nearby packs
        int count = 0;
        for(LivingEntity bukkitentity : location.getNearbyLivingEntities(packsearchhorizontal, packsearchvertical)) {
            Entity entity = DataManager.instance().entity(bukkitentity.getUniqueId());
            if(entity != null && entity.pack() != null && entity.pack().leader() == entity) {
                ++count;
            }
        }

        // make sure there are not too many packs nearby
        if(count > packcount) {
            // return failure
            return false;
        }

        // get the entity
        Entity entity = DataManager.instance().entity(livingentity);
        if(entity == null) {
            // return failure
            return false;
        }

        // create the entity pack
        PackManager.instance().create(entity);
        return true;
    }
}
