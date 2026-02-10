package net.mctitan.rpg.handler.general;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    private static final PlayerJoin instance = new PlayerJoin();

    private PlayerJoin() {}

    public static PlayerJoin instance() { return instance; }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // get player object
        Player player = DataManager.instance().player(event.getPlayer());

        // setup scheduled tasks for players
        player.actionbar().player(player);
        player.mana().player(player);
    }
}
