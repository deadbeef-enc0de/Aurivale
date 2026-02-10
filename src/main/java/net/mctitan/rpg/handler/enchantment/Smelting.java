package net.mctitan.rpg.handler.enchantment;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Smelting implements Listener {
    private static final Smelting instance = new Smelting();
    private Map<Material, Material> smeltables = new HashMap<>();

    private Smelting() {
        Iterator<Recipe> recipes = Bukkit.recipeIterator();
        while(recipes.hasNext()) {
            Recipe recipe = recipes.next();
            if(!(recipe instanceof FurnaceRecipe smeltrecipe)) {
                continue;
            }

            Material output = smeltrecipe.getResult().getType();
            if(smeltrecipe.getInputChoice() instanceof RecipeChoice.MaterialChoice matchoice) {
                for(Material input : matchoice.getChoices()) {
                    smeltables.put(input, output);
                }
            }
        }
    }

    public static Smelting instance() { return instance; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if(event.isCancelled()) {
            return;
        }

        // get some intial info
        Player player = DataManager.instance().player(event.getPlayer());
        ItemStack inhand = new ItemStack(event.getPlayer().getEquipment().getItemInMainHand());
        int smelting = player.enchantments().level(Enchantment.smelting);
        int itembag = player.enchantments().level(Enchantment.item_bag);

        // if there is no smelting, move on
        if(smelting < 1) {
            return;
        }

        // setup data to hold item stacks to drop independently of this event
        Map<Material, org.bukkit.inventory.ItemStack> stacks = new HashMap<>();

        // go through each item to check to see if it should smelt or not
        Iterator<Item> iterator = event.getItems().iterator();
        while(iterator.hasNext()) {
            Item item = iterator.next();

            // check to see if the item is smeltable
            if(!smeltables.containsKey(item.getItemStack().getType())) {
                continue;
            }

            // loop through the amount in the item stack and do each item
            // this is because similar items are pre-grouped together
            int remove = 0;
            for(int i = 0; i < item.getItemStack().getAmount(); ++i) {
                if(Math.nextInt(5) < smelting) {
                    Material smelted = smeltables.get(item.getItemStack().getType());
                    if(!stacks.containsKey(smelted)) {
                        stacks.put(smelted, new org.bukkit.inventory.ItemStack(smelted));
                    } else {
                        org.bukkit.inventory.ItemStack stack = stacks.get(smelted);
                        stack.setAmount(stack.getAmount() + 1);
                    }
                    ++remove;
                }
            }

            // if the itemstack is empty, remove this iterator
            if(item.getItemStack().getAmount() == remove) {
                iterator.remove();
            } else {
                item.getItemStack().setAmount(item.getItemStack().getAmount() - remove);
            }
        }

        // drop the additional item stacks or check for item bag enchantment
        Location location = new Location(event.getBlock().getLocation());
        for(org.bukkit.inventory.ItemStack stack : stacks.values()) {
            if(itembag > 0) {
                ItemBag.bagitems(player, location, stack);
            } else {
                location.world().dropItemNaturally(location.center().bukkit(), stack);
            }
        }
    }
}
