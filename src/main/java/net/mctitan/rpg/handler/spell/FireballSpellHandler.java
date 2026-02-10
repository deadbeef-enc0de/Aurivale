package net.mctitan.rpg.handler.spell;

import net.mctitan.rpg.spell.Spells;
import net.mctitan.rpg.spell.projectile.SpellProjectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class FireballSpellHandler implements Listener {
    private static final FireballSpellHandler instance = new FireballSpellHandler();

    private  FireballSpellHandler() {}

    public static FireballSpellHandler instance() { return instance; }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // get spell projectile
        SpellProjectile projectile = Spells.instance().projectile(event.getEntity().getUniqueId());
        if(projectile == null) { return; }

        // cancel the miencraft event
        event.setCancelled(true);

        // remove the projectile which will call the effect method on the spell instance
        projectile.remove();
    }
}
