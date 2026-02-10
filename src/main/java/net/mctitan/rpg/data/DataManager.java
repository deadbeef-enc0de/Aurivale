package net.mctitan.rpg.data;

import net.mctitan.data.BaseData;
import net.mctitan.data.UUID;
import net.mctitan.data.saver.PluginSaver;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.pack.EntityPack;
import net.mctitan.rpg.visual.Visuals;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataManager {
    private static DataManager INSTANCE = new DataManager();

    private PluginSaver data;

    // main data objects
    private HashMap<UUID, Entity> entities = new HashMap<>();
    private HashMap<UUID, Player> players = new HashMap<>();
    private HashMap<UUID, EntityPack> packs = new HashMap<>();
    private HashMap<UUID, Party> parties = new HashMap<>();

    private DataManager() {
        data = PluginSaver.saver(Aurivale.instance());
    }

    public static DataManager instance() { return INSTANCE; }

    /**
     * Saves object to disk
     * @param bd data to save to disk
     */
    public void save(BaseData bd) {
        synchronized (data) {
            data.save(bd);
        }
    }

    /**
     * Removes the entity or player from the server
     * @param uuid unique id of the entity or player
     */
    public void remove(java.util.UUID uuid) { remove(new UUID(uuid)); }

    /**
     * Removes the entity or player from the server
     * @param uuid unique id of the entity or player
     */
    public void remove(UUID uuid) {
        // lock and try to remove entity
        synchronized (entities) {
            if(entities.containsKey(uuid)) {
                // remove entity from cache
                Entity entity = entities.remove(uuid);

                // remove entity visual
                Visuals.instance().remove(entity);

                // delete data from disk
                synchronized (data) { data.remove(entity); }
            }
        }

        // lock and try to remove player
        synchronized (players) {
            if(players.containsKey(uuid)) {
                Player player = players.remove(uuid);
                synchronized (data) { data.remove(player); }
            }
        }

        // lock and try to remove entity pack
        synchronized (packs) {
            if(packs.containsKey(uuid)) {
                // remove the entity pack
                EntityPack pack = packs.remove(uuid);
                synchronized (data) { data.remove(pack); }

                // remove pack link from leader
                if(pack.leader() != null) {
                    pack.leader().pack(null);
                    save(pack.leader());
                }

                // remove pack from followers
                for(Entity follower : pack.followers()) {
                    follower.pack(null);
                    save(follower);
                }
            }
        }

        // lock and try to remove player party
        synchronized (parties) {
            if(parties.containsKey(uuid)) {
                // remove the party
                Party party = parties.remove(uuid);
                synchronized (data) { data.remove(party); }

                // remove owner from party
                party.owner().party(null);
                save(party.owner());

                // remove members from party
                for(Player member : party.members()) {
                    member.party(null);
                    save(member);
                }
            }
        }
    }

    /**
     * Attempts to load the object from disk
     * @param uuid unique id of the data to load
     * @param clazz what type of data to load
     * @return data if it exists, null otherwise
     * @param <T> type of data that extends BaseData
     */
    public <T extends BaseData> T load(UUID uuid, Class<T> clazz) {
        synchronized (data) {
            return data.load(uuid, clazz);
        }
    }

    public void unload(EntityPack pack) {
        if(pack == null) { return; }

        synchronized (packs) {
            packs.remove(pack.uuid);
        }
    }

    /**
     * Saves all pending player and entity data when plugin comes down
     */
    public void disable() {
        for(Map.Entry<UUID, Entity> entry : entities.entrySet()) {
            entry.getValue().disable();
            entry.getValue().projectiles().removeall();
            save(entry.getValue());
        }

        for(Map.Entry<UUID, Player> entry : players.entrySet()) {
            entry.getValue().disable();
            entry.getValue().projectiles().removeall();
            save(entry.getValue());
        }
    }

    /**
     * Gets or creates a new entity data object
     * @param bukkitentity the bukkit entity handle
     * @return the entity data object
     */
    public Entity entity(org.bukkit.entity.LivingEntity bukkitentity) {
        // if the entity is actually a player, do that code instead
        if(bukkitentity instanceof org.bukkit.entity.Player player) {
            return player(player);
        }

        Entity ret = null;
        synchronized(entities) {
            // detect if entity exists, if so return it
            ret = entity(bukkitentity.getUniqueId());
            if (ret != null) {
                return ret;
            }

            // if the entity has 0 health or is dead, don't create entity object
            if(bukkitentity.isDead() || bukkitentity.getHealth() <= 0) {
                return null;
            }

            // make new entity and save it
            ret = new Entity(bukkitentity);
            entities.put(ret.uuid, ret);
            save(ret);
            ret.initialize();
        }

        return ret;
    }

    /**
     * Gets the entity object by uuid
     * @param uuid entity's uuid
     * @return the entity object if found, null otherwise
     */
    public Entity entity(java.util.UUID uuid) { return entity(new UUID(uuid)); }

    /**
     * Gets the entity object by uuid
     * @param uuid entity's uuid
     * @return the entity object if found, null otherwise
     */
    public Entity entity(UUID uuid) {
        // try get get the player object first
        Player player = player(uuid);
        if(player != null) {
            return player;
        }

        // if not try to get the entity
        synchronized (entities) {
            if(entities.containsKey(uuid)) {
                return entities.get(uuid);
            }

            Entity ret = load(uuid, Entity.class);
            if(ret != null) {
                ret.initialize();
                entities.put(uuid, ret);
            }
            return ret;
        }
    }

    /**
     * Gets or creates a new player data object
     * @param bukkitplayer the bukkit player handle
     * @return the player data object
     */
    public Player player(org.bukkit.entity.Player bukkitplayer) {
        Player ret = null;
        synchronized (players) {
            // detect if player already exists, if so return it
            ret = player(bukkitplayer.getUniqueId());
            if (ret != null) {
                return ret;
            }

            // make new player and save it
            ret = new Player(bukkitplayer);
            players.put(ret.uuid, ret);
            save(ret);
            ret.initialize();
        }

        return ret;
    }

    /**
     * Gets the player object by uuid
     * @param uuid player's uuid
     * @return the player object if found, null otherwise
     */
    public Player player(java.util.UUID uuid) { return player(new UUID(uuid)); }

    /**
     * Gets the player object by uuid
     * @param uuid player's uuid
     * @return the player object if found, null otherwise
     */
    public Player player(UUID uuid) {
        synchronized(players) {
            if(players.containsKey(uuid)) {
                return players.get(uuid);
            }

            Player ret = load(uuid, Player.class);
            if(ret != null) {
                ret.initialize();
                players.put(ret.uuid, ret);
            }
            return ret;
        }
    }

    public Set<EntityPack> packs() { return new HashSet<>(packs.values()); }

    /**
     * Gets or creates a new entity pack data object
     * @param bukkitentity bukkit entity handle
     * @return entity pack data object
     */
    public EntityPack pack(LivingEntity bukkitentity) { return pack(entity(bukkitentity)); }

    /**
     * Gets or creates a new entity pack data object
     * @param leader rpg entity handle
     * @return entity pack data object
     */
    public EntityPack pack(Entity leader) {
        // synchronize the packs
        synchronized (packs) {
            // try to get an existing pack
            EntityPack ret = leader.pack();
            if(ret != null) {
                return ret;
            }

            // pack doesn't exist, make one
            ret = new EntityPack(leader);
            packs.put(ret.uuid, ret);
            save(ret);
            save(leader);

            return ret;
        }
    }

    /**
     * Gets the entity pack object
     * @param uuid entity pack's uuid
     * @return entity pack object if found, null otherwise
     */
    public EntityPack pack(java.util.UUID uuid) { return pack(new UUID(uuid)); }

    /**
     * Gets the entity pack object
     * @param uuid entity pack's uuid
     * @return entity pack object if found, null otherwise
     */
    public EntityPack pack(UUID uuid) {
        synchronized (packs) {
            if(packs.containsKey(uuid)) {
                return packs.get(uuid);
            }

            EntityPack ret = load(uuid, EntityPack.class);
            if(ret != null) {
                packs.put(uuid, ret);
            }
            return ret;
        }
    }

    /**
     * Gets or creates a new player party data object
     * @param player rpg player handle
     * @return player party data object
     */
    public Party party(Player player) {
        synchronized (parties) {
            // try to get existing party
            Party ret = player.party();
            if(ret != null) {
                return ret;
            }

            // party doesn't exist, make one
            ret = new Party(player);
            parties.put(ret.uuid, ret);
            player.party(ret);
            save(ret);
            save(player);

            return ret;
        }
    }

    public Party party(java.util.UUID uuid) { return party(new UUID(uuid)); }

    /**
     * Gets the player party object
     * @param uuid player party's uuid
     * @return player party object if found, null otherwise
     */
    public Party party(UUID uuid) {
        synchronized (parties) {
            if(parties.containsKey(uuid)) {
                return parties.get(uuid);
            }

            Party ret = load(uuid, Party.class);
            if(ret != null) {
                parties.put(uuid, ret);
            }
            return ret;
        }
    }
}
