package net.mctitan.rpg.handler.enchantment;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public class StraightShot implements Listener {
    private static StraightShot instance = new StraightShot();

    private StraightShot() {}

    public static StraightShot instance() { return instance; }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        // make sure projectile is an arrow
        if(!(event.getProjectile() instanceof Arrow arrow)) {
            return;
        }

        // get some details
        Entity entity = DataManager.instance().entity(event.getEntity());

        // check for straight shot enchantment
        if(entity.enchantments().level(Enchantment.straight_shot) <= 0) {
            return;
        }

        // remove gravity
        arrow.setGravity(false);

        // remove arrow if it gets too slow, check every second
        arrow.getScheduler().runAtFixedRate(Aurivale.instance(), task -> {
            Vector velocity = new Vector(arrow.getVelocity());
            if(arrow.getAttachedBlock() == null && velocity.magnitude() < 0.01) {
                // remove projectile from shooter and despawn it
                entity.projectiles().remove(arrow.getUniqueId());
                arrow.remove();
            }
        }, null, 20, 20);
    }
}
