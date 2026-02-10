package net.mctitan.rpg.handler.general;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.util.directional.ChunkLocation;
import net.mctitan.rpg.visual.Visuals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class VisualLoading implements Listener {
    private static final VisualLoading instance = new VisualLoading();

    private VisualLoading() {}

    public static VisualLoading instance() { return instance; }

    @EventHandler
    public void onEntityAddToWorld(EntityAddToWorldEvent event) {
        Entity entity = DataManager.instance().entity(event.getEntity().getUniqueId());
        if(entity == null) { return; }

        Visuals.instance().entityadd(entity);
    }

    @EventHandler
    public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        Entity entity = DataManager.instance().entity(event.getEntity().getUniqueId());
        if(entity == null) { return; }

        Visuals.instance().entityremove(entity);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = DataManager.instance().entity(event.getEntity().getUniqueId());
        if(entity == null) { return; }

        Visuals.instance().remove(entity);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        ChunkLocation chunklocation = new ChunkLocation(event.getChunk());
        Visuals.instance().chunkload(chunklocation);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        ChunkLocation chunklocation = new ChunkLocation(event.getChunk());
        Visuals.instance().chunkunload(chunklocation);
    }
}
