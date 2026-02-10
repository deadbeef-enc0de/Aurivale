package net.mctitan.rpg.event;

import net.mctitan.rpg.data.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerStatsUpdateEvent extends Event {
    private final Player player;

    public PlayerStatsUpdateEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() { return player; }

    private static final HandlerList HANDLERS = new HandlerList();
    public static HandlerList getHandlerList() { return HANDLERS; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
}
