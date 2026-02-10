package net.mctitan.rpg.handler.general;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.teleporter.sequence.RandomWorldLocationSequence;
import net.mctitan.rpg.util.teleporter.Teleporter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerRespawn implements Listener, Logger {
    private static final PlayerRespawn instance = new PlayerRespawn();
    private final int respawnaggroprotect;

    // TODO remove when manual respawn is no longer needed
    private HashMap<UUID, Double> health = new HashMap<>();

    private PlayerRespawn() {
        // get aggro protection for respawn
        respawnaggroprotect = Aurivale.instance().getConfig("players").getInt("respawn_aggro_protection");

        // TODO remove this when respawn event is working
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(Aurivale.instance(), task -> {
            // go through each online player
            for(org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                // get player uuid
                UUID playerid = player.getUniqueId();

                // check to see if they went from dead to alive
                if(health.containsKey(playerid) && health.get(playerid) <= 0 && player.getHealth() > 0) {
                    Location bedloc = player.getPotentialBedLocation();
                    PlayerRespawnEvent respawnevent = new PlayerRespawnEvent(
                            player,
                            bedloc == null ? player.getLocation() : bedloc,
                            bedloc != null
                    );
                    respawnevent.callEvent();
                }

                // set the players dead state
                health.put(playerid, player.getHealth());
            }
        }, 1, 1);
    }

    public static PlayerRespawn instance() { return instance; }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // check to see if the player is new
        Player player = DataManager.instance().player(event.getPlayer());
        if(!player.isnew()) {
            return;
        }

        player.isnew(false);
        teleport(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        log(Level.INFO, "RandomSpawn::onPlayerRespawn player="+event.getPlayer().getName());
        // check to see if event is a bed spawn
        if(event.isBedSpawn()) {
            return;
        }

        // get player
        Player player = DataManager.instance().player(event.getPlayer());

        // reset equipment modifiers on player
        player.bukkitplayer().getScheduler().run(Aurivale.instance(), task -> {
            // reset equipment modifiers
            player.equipment().resetmodifiers(player);

            // update player health
            player.health(player.bukkitplayer().getHealth());

            // set player object on mana
            player.mana().player(player);

            // update player aggro protection
            player.setaggroprotection(respawnaggroprotect);
            }, null);

        // set the teleporter and the event respawn location
        Teleporter teleporter = teleport(player);
        event.setRespawnLocation(teleporter.current().bukkit());
    }

    private Teleporter teleport(Player player) {
        Teleporter teleporter = Teleporter.teleport(player, new RandomWorldLocationSequence());
        teleporter.water(false);
        teleporter.lava(false);

        return teleporter;
    }
}
