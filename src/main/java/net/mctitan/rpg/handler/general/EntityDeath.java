package net.mctitan.rpg.handler.general;

import net.mctitan.rpg.data.DataManager;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeath implements Listener {
    private static final EntityDeath instance = new EntityDeath();

    private EntityDeath() {}

    public static EntityDeath instance() { return instance; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if(event.getEntity().getType() == EntityType.PLAYER) {
            return;
        }

        DataManager.instance().remove(event.getEntity().getUniqueId());
    }
}
