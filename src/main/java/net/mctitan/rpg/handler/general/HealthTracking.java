package net.mctitan.rpg.handler.general;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.event.PlayerStatsUpdateEvent;
import net.mctitan.rpg.util.Logger;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.logging.Level;

public class HealthTracking implements Listener, Logger {
    private static final HealthTracking instance = new HealthTracking();

    private HealthTracking() {}

    public static HealthTracking instance() { return instance; }

    @EventHandler
    public void onHealthRegen(EntityRegainHealthEvent event) {
        // get living entity
        if(!(event.getEntity() instanceof LivingEntity bukkitentity)) {
            return;
        }

        // if cause is custom, do nothing
        if(event.getRegainReason() == EntityRegainHealthEvent.RegainReason.CUSTOM) {
            return;
        }

        // get entity and heal
        Entity entity = DataManager.instance().entity(bukkitentity);
        entity.heal(event.getAmount());

        // cancel original event
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageWatch(EntityDamageEvent event) {
        // skip cancelled damage
        if(event.isCancelled() || event.getCause() == EntityDamageEvent.DamageCause.CUSTOM) { return; }

        // get entity without creating one
        Entity entity = DataManager.instance().entity(event.getEntity().getUniqueId());
        if(entity == null) {
            return;
        }

        // update health
        entity.health(entity.health() - event.getFinalDamage());
        log(Level.INFO, String.format("%s took %.01f %s damage updated health=%.01f",
                entity.name(),
                event.getFinalDamage(),
                event.getCause(),
                entity.health()
        ));

        // have player stat scoreboard update
        if(event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) entity;
            PlayerStatsUpdateEvent statupdateevent = new PlayerStatsUpdateEvent(player);
            statupdateevent.callEvent();
        }
    }
}
