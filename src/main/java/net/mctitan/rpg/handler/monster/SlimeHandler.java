package net.mctitan.rpg.handler.monster;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.enums.ActionName;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SlimeHandler implements Listener {
    private static final SlimeHandler instance = new SlimeHandler();
    private static final int SLIME_HIT_TIME = 20; // in servers ticks

    private Map<UUID, Map<UUID, Integer>> slimehits = new HashMap<>();

    private SlimeHandler() {
        // setup small slime attacks
        smallslimes();
    }

    public static SlimeHandler instance() { return instance; }

    @EventHandler
    public void onSlimeDamage(EntityDamageByEntityEvent event) {
        // make sure the damager is a slime
        if(event.getDamager().getType() != EntityType.SLIME &&
           event.getDamager().getType() != EntityType.MAGMA_CUBE) {
            return;
        }

        // get entities
        Entity slime = event.getDamager();
        Entity entity = event.getEntity();

        // get last hit time
        if(!slimehits.containsKey(slime.getUniqueId())) { slimehits.put(slime.getUniqueId(), new HashMap<>()); }
        Integer time = slimehits.get(slime.getUniqueId()).get(entity.getUniqueId());

        // if it hasn't been long enough, cancel the event
        if(time != null && entity.getTicksLived() <= time) {
            event.setCancelled(true);
            return;
        }

        // set the time for the next allowed attack
        slimehits.get(slime.getUniqueId()).put(entity.getUniqueId(), entity.getTicksLived() + SLIME_HIT_TIME);
    }

    private void smallslimes() {
        // every 0.25s check all players to see if small slimes hit them
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(Aurivale.instance(), task -> {
            for(Player player : Bukkit.getOnlinePlayers()) {
                // get center of bounding box for player
                Location location = player.getBoundingBox().getCenter().toLocation(player.getWorld());

                // get nearby entities to player bounding box in it's scheduler
                Bukkit.getRegionScheduler().run(Aurivale.instance(), location, locationTask -> {
                    for(Slime slime : location.getNearbyEntitiesByType(Slime.class, 0.3, 0.9)) {
                        // make sure slime is the correct size
                        if(ActionName.slime(slime.getSize()) != ActionName.SLIME_SMALL) { continue; }

                        // send out entity damage by entity event
                        // @TODO replace with non-deprecated ctor
                        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(slime, player, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1);
                        event.callEvent();
                    }
                });
            }
        }, 5, 5);
    }
}
