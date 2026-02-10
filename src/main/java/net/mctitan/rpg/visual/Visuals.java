package net.mctitan.rpg.visual;

import net.mctitan.data.BaseData;
import net.mctitan.data.UUID;
import net.mctitan.data.saver.PluginSaver;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.VisualType;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.directional.ChunkLocation;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class Visuals implements Logger {
    private static final Visuals instance = new Visuals();

    private PluginSaver data;
    private HashMap<UUID, Visual> visuals = new HashMap<>();
    private HashMap<ChunkLocation, HashSet<Visual>> locations = new HashMap<>();
    private HashMap<UUID, HashSet<Visual>> entities = new HashMap<>();
    private HashSet<Visual> loaded = new HashSet<>();

    private Visuals() {}

    public static Visuals instance() { return instance; }
    public static void initialize() { instance.load(); }

    private void load() {
        // get yaml saver
        data = PluginSaver.saver(Aurivale.instance());

        // load all visuals from disk by class
        for(VisualType type : VisualType.values()) {
            log(Level.INFO, String.format("Loading visuals for type=%s", type.name()));
            data.loadall(type.visualclass());
            for(BaseData bd : data.get(type.visualclass())) {
                Visual visual = (Visual) bd;

                // add to visuals
                visuals.put(visual.uuid,  visual);

                // add to locations
                if(visual instanceof LocationVisual locationvisual) {
                    ChunkLocation chunklocation = locationvisual.location().chunklocation();
                    if(!locations.containsKey(chunklocation)) { locations.put(chunklocation, new HashSet<>()); }
                    locations.get(chunklocation).add(visual);
                }

                // add to entities
                else if(visual instanceof EntityVisual entityvisual) {
                    UUID uuid = entityvisual.entityuuid();
                    if(!entities.containsKey(uuid)) { entities.put(uuid, new HashSet<>()); }
                    entities.get(uuid).add(visual);
                }

                // initialize visual
                visual.initialize();
            }
        }

        // setup scheduler
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(Aurivale.instance(), task -> {
            // copy loaded visuals
            HashSet<Visual> tick = new HashSet<>();
            synchronized (loaded) { tick.addAll(loaded); }

            // tick each of the visuals in their scheduler
            for(Visual visual : tick) {
                // make sure the visual has a location to schedule in
                if(visual.location() == null) { continue; }

                // schedule visual tick in its scheduler
                Bukkit.getRegionScheduler().run(Aurivale.instance(), visual.location().bukkit(), visualtask -> {
                    visual.tick();
                });
            }
        }, 1, 1);
    }

    public Visual get(UUID uuid) { synchronized (visuals) { return visuals.get(uuid); } }

    public Set<Visual> get(ChunkLocation location) {
        HashSet<Visual> ret = new HashSet<>();
        synchronized (locations) {
            if(locations.containsKey(location)) {
                ret.addAll(locations.get(location));
            }
        }

        return ret;
    }

    public Set<Visual> get(Entity entity) {
        HashSet<Visual> ret = new HashSet<>();
        synchronized (entities) {
            if(entities.containsKey(entity.uuid)) {
                ret.addAll(entities.get(entity.uuid));
            }
        }

        return ret;
    }

    public void add(Visual visual) {
        // add to visuals
        synchronized (visuals) { visuals.put(visual.uuid,  visual); }

        // add location visual
        if(visual instanceof LocationVisual locactionvisual) {
            ChunkLocation location = locactionvisual.location().chunklocation();
            synchronized (locations) {
                if(!locations.containsKey(location)) { locations.put(location, new HashSet<>()); }
                locations.get(location).add(locactionvisual);
            }
        }

        // add entity visual
        else if(visual instanceof EntityVisual entityvisual) {
            Entity entity = entityvisual.entity();
            synchronized (entities) {
                if(!entities.containsKey(entity.uuid)) { entities.put(entity.uuid, new HashSet<>()); }
                entities.get(entity.uuid).add(entityvisual);
            }
        }

        // add to loaded visuals
        synchronized (loaded) { loaded.add(visual); }

        // save visual to disk
        data.save(visual);
    }

    public void remove(Entity entity) {
        for(Visual visual : get(entity)) {
            remove(visual);
        }
    }

    public void remove(Visual visual) {
        // remove visual from disk
        data.remove(visual);

        // remove from loaded visuals
        synchronized (loaded) { loaded.remove(visual); }

        // remove location visual
        if(visual instanceof LocationVisual locactionvisual) {
            ChunkLocation location = locactionvisual.location().chunklocation();
            synchronized (locations) {
                locations.get(location).remove(locactionvisual);
                if(locations.get(location).isEmpty()) {
                    locations.remove(location);
                }
            }
        }

        // remove entity visual
        if(visual instanceof EntityVisual entityvisual) {
            Entity entity = entityvisual.entity();
            synchronized (entities) {
                entities.get(entity.uuid).remove(entityvisual);
                if(entities.get(entity.uuid).isEmpty()) {
                    entities.remove(entity.uuid);
                }
            }
        }

        // remove from all visuals
        synchronized (visuals) { visuals.remove(visual.uuid); }
    }

    public void entityadd(Entity entity) {
        // get visuals for chunk
        Set<Visual> load = get(entity);
        if(load.isEmpty()) { return; }

        // add to loaded list
        synchronized (loaded) { loaded.addAll(load); }
    }

    public void entityremove(Entity entity) {
        // get visuals for chunk
        Set<Visual> unload = get(entity);
        if(unload.isEmpty()) { return; }

        // add to loaded list
        synchronized (loaded) { loaded.removeAll(unload); }
    }

    public void chunkload(ChunkLocation chunklocation) {
        // get visuals for chunk
        Set<Visual> load = get(chunklocation);
        if(load.isEmpty()) { return; }

        // add to loaded list
        synchronized (loaded) { loaded.addAll(load); }
    }

    public void chunkunload(ChunkLocation chunklocation) {
        // get visuals for chunk
        Set<Visual> unload = get(chunklocation);
        if(unload.isEmpty()) { return; }

        // add to loaded list
        synchronized (loaded) { loaded.removeAll(unload); }
    }
}
