package net.mctitan.rpg.handler.general;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class NoExpDrop implements Listener {
    private static final NoExpDrop instance = new NoExpDrop();

    private NoExpDrop() {}

    public static NoExpDrop instance() { return instance; }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.EXPERIENCE_ORB) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExp(BlockExpEvent event) {
        event.setExpToDrop(0);
    }

    @EventHandler
    public void onEntityBreed(EntityBreedEvent event) {
        event.setExperience(0);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.setDroppedExp(0);
    }
}
