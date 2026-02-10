package net.mctitan.rpg.handler.general;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;

public class DisabledFeatures implements Listener {
    private static final DisabledFeatures instance = new DisabledFeatures();

    private DisabledFeatures() {
        Iterator<Recipe> recipes = Bukkit.recipeIterator();
        while(recipes.hasNext()) {
            Recipe recipe = recipes.next();
            if(recipe.getResult().getType() == Material.GRINDSTONE ||
                    recipe.getResult().getType() == Material.SMITHING_TABLE ||
                    recipe.getResult().getType() == Material.FISHING_ROD) {
                Keyed keyed = (Keyed)recipe;
                Bukkit.removeRecipe(keyed.getKey());
            }
        }
    }

    public static DisabledFeatures instance() { return instance; }

    @EventHandler
    public void onBadPlayerInteract(PlayerInteractEvent event) {
        if(!event.hasBlock() || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Material material = event.getClickedBlock().getType();
        if(material == Material.GRINDSTONE ||
                material == Material.ENCHANTING_TABLE ||
                material == Material.SMITHING_TABLE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPiglinBarter(PiglinBarterEvent event) {
        event.setCancelled(true);
    }
}
