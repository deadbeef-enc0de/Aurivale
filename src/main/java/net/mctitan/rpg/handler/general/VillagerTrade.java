package net.mctitan.rpg.handler.general;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enums.ItemType;
import net.mctitan.rpg.enums.RandomType;
import net.mctitan.rpg.trading.Profession;
import net.mctitan.rpg.trading.Trading;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.inventory.MerchantRecipe;

public class VillagerTrade implements Listener {
    private static final VillagerTrade instance = new VillagerTrade();

    private VillagerTrade() {}

    public static VillagerTrade instance() { return instance; }

    @EventHandler
    public void onVillagerCareerChange(VillagerCareerChangeEvent event) {
        // get entity
        Entity entity = DataManager.instance().entity(event.getEntity());

        // reset villager trade random generator
        entity.randoms().random(RandomType.VILLAGER_TRADE).reset();
    }

    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if(event.getEntityType() != EntityType.VILLAGER) {
            if(event.getEntityType() != EntityType.WANDERING_TRADER) { event.setCancelled(true); }
            return;
        }

        Villager villager = (Villager)event.getEntity();
        Profession profession = Trading.instance().profession(villager.getProfession());
        if(profession == null) {
            event.setCancelled(true);
            return;
        }

        Entity entity = DataManager.instance().entity(event.getEntity());
        MerchantRecipe recipe = recipe(entity, event.getEntity(), profession);
        event.setRecipe(recipe);
    }

    @EventHandler
    public void onWanderingVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if(event.getEntityType() != EntityType.WANDERING_TRADER) {
            if(event.getEntityType() != EntityType.VILLAGER) { event.setCancelled(true); }
            return;
        }

        Profession profession = Trading.instance().profession("wandering_trader");
        Entity entity = DataManager.instance().entity(event.getEntity());
        MerchantRecipe recipe = recipe(entity, event.getEntity(), profession);
        event.setRecipe(recipe);
    }

    private MerchantRecipe recipe(Entity entity, AbstractVillager villager, Profession profession) {
        // keep trying to get a recipe
        MerchantRecipe ret = null;
        do {
            // get potential recipe
            MerchantRecipe rolledrecipe = profession.recipe(entity);
            ItemStack rolledstack = new ItemStack(rolledrecipe.getResult());

            // go through all existing recipes and make sure there are not any duplicates
            boolean good = true;
            for(MerchantRecipe recipe : villager.getRecipes()) {
                ItemStack existing = new ItemStack(recipe.getResult());

                // rolled recipe is a craftable
                if(rolledstack.craftable() != null &&
                   rolledstack.type() == ItemType.NORMAL && existing.type() == ItemType.NORMAL &&
                   rolledstack.craftable() == existing.craftable()) {
                    good = false;
                    break;
                }

                // rolled recipe is en enchanter
                if(rolledstack.enchantertype() != null && rolledstack.enchantertype() == existing.enchantertype()) {
                    good = false;
                    break;
                }

                // rolled recipe is an item
                else if(rolledstack.craftable() == null && rolledstack.enchantertype() == null &&
                        rolledstack.bukkitstack().getType() == existing.bukkitstack().getType()) {
                    good = false;
                    break;
                }
            }

            if(good) {
                ret = rolledrecipe;
            }

        } while(ret == null);

        // return the recipe
        return ret;
    }
}
