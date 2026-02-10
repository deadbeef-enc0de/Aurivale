package net.mctitan.rpg.event;

import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.util.stats.PlayerStats;
import net.mctitan.rpg.util.stats.Stats;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerStatScoreboardUpdateEvent extends Event {
    private final org.bukkit.entity.Player player;
    private Stats stats;

    public PlayerStatScoreboardUpdateEvent(Player player) {
        this.player = player.bukkitplayer();
        this.stats = new PlayerStats(player);
    }

    public org.bukkit.entity.Player getPlayer() { return player; }
    public Stats getStats() { return stats; }

    private static final HandlerList HANDLERS = new HandlerList();
    public static HandlerList getHandlerList() { return HANDLERS; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
}
