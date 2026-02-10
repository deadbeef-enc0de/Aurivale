package net.mctitan.rpg.handler.general;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.event.PlayerStatScoreboardUpdateEvent;
import net.mctitan.rpg.event.PlayerStatsUpdateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerStatScoreboard implements Listener {
    private static final PlayerStatScoreboard instance = new PlayerStatScoreboard();

    private PlayerStatScoreboard() {}

    public static PlayerStatScoreboard instance() { return instance; }

    @EventHandler
    public void onSendPlayerStats(PlayerStatsUpdateEvent event) {
        event.getPlayer().bukkitplayer().getScheduler().run(Aurivale.instance(), task -> {
            PlayerStatScoreboardUpdateEvent scoreboardevent = new PlayerStatScoreboardUpdateEvent(event.getPlayer());
            scoreboardevent.callEvent();
        }, null);
    }
}
