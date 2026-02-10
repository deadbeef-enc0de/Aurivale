package net.mctitan.rpg.handler.trigger;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.TriggerType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import java.util.Collection;

public class ModifierTriggers implements Listener {
    private static final ModifierTriggers instance = new ModifierTriggers();

    private ModifierTriggers() {}

    public static ModifierTriggers instance() { return instance; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSneakToggle(PlayerToggleSneakEvent event) {
        Player player = DataManager.instance().player(event.getPlayer());

        if(event.isSneaking()) {
            player.actions().trigger().activate(TriggerType.SNEAKING);
            player.actions().trigger().deactivate(TriggerType.NOT_SNEAKING);
        } else {
            player.actions().trigger().deactivate(TriggerType.SNEAKING);
            player.actions().trigger().activate(TriggerType.NOT_SNEAKING);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSprintToggle(PlayerToggleSprintEvent event) {
        Player player = DataManager.instance().player(event.getPlayer());

        if(event.isSprinting()) {
            player.actions().trigger().activate(TriggerType.SPRINTING);
            player.actions().trigger().deactivate(TriggerType.NOT_SPRINTING);
        } else {
            player.actions().trigger().deactivate(TriggerType.SPRINTING);
            player.actions().trigger().activate(TriggerType.NOT_SPRINTING);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBowShoot(EntityShootBowEvent event) {
        Entity entity = DataManager.instance().entity(event.getEntity());
        entity.actions().trigger().activate(TriggerType.BOW_SHOT);
    }
}
