package net.mctitan.rpg.handler.monster;

import com.destroystokyo.paper.event.entity.WitchConsumePotionEvent;
import com.destroystokyo.paper.event.entity.WitchThrowPotionEvent;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.action.potion.PotionAction;
import net.mctitan.rpg.data.potion.PotionInfo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.PotionMeta;

public class WitchHandler implements Listener {
    private static final WitchHandler instance = new WitchHandler();

    private WitchHandler() {}

    public static WitchHandler instance() { return instance; }

    @EventHandler
    public void onWitchConsumePotion(WitchConsumePotionEvent event) {
        // get the entity
        Entity witch = DataManager.instance().entity(event.getEntity());
        if(witch == null) {
            return;
        }

        // get the potion drank
        PotionMeta meta = (PotionMeta)event.getPotion().getItemMeta();
        PotionAction action = witch.actions().potion(meta.getBasePotionType());
        ItemStack potion = (action != null ? action.potion() : null);
        if(potion == null) {
            return;
        }

        PotionInfo potioninfo = new PotionInfo(potion.modifiers());
        potioninfo.apply(witch);
    }

    @EventHandler
    public void onWitchThrowPotion(WitchThrowPotionEvent event) {
        Entity witch = DataManager.instance().entity(event.getEntity());
        PotionMeta meta = (PotionMeta)event.getPotion().getItemMeta();
        PotionAction action = witch.actions().potion(meta.getBasePotionType());
        ItemStack potion = (action != null ? action.potion() : null);
        if(potion == null) {
            event.setCancelled(true);
            return;
        }

        event.setPotion(potion.bukkitstack());
    }
}
