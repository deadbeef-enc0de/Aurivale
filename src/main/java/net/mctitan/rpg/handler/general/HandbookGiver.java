package net.mctitan.rpg.handler.general;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.handbook.Handbook;
import net.mctitan.rpg.util.comparators.VersionComparator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class HandbookGiver implements Listener {
    private static final HandbookGiver instance = new HandbookGiver();
    private final Handbook handbook = Handbook.instance();

    private HandbookGiver() {}

    public static HandbookGiver instance() { return instance; }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // get player
        Player player = DataManager.instance().player(event.getPlayer());

        // if the player doesn't have a handbook or the handbook is newer, give them one
        if(player.handbook() == null || VersionComparator.COMPARE(handbook.version(), player.handbook()) > 0) {
            player.bukkitplayer().getInventory().addItem(handbook.item().bukkitstack());
            player.handbook(handbook.version());
        }
    }
}
