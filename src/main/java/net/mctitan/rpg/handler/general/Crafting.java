package net.mctitan.rpg.handler.general;

import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.comparators.ItemStackComparator;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.*;
import java.util.logging.Level;

public class Crafting implements Listener, Logger {
    private static final Crafting instance = new Crafting();

    private Crafting() {}

    public static Crafting instance() { return instance; }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack current = new ItemStack(event.getCurrentItem());
        Craftable craftable = current.craftable();
        if(craftable == null) {
            return;
        }

        // get crafting roll
        Player player = DataManager.instance().player((org.bukkit.entity.Player) event.getWhoClicked());
        int crafting;

        // Shift Click Crafting
        if(event.isShiftClick()) {
            // Shaped Recipe
            if(event.getRecipe() instanceof ShapedRecipe) {
                ShapedRecipe recipe = (ShapedRecipe) event.getRecipe();

                // get items in crafting matrix
                Map<Material, TreeSet<ItemStack>> matrix = new HashMap<>();
                for(int index = 0; index < event.getInventory().getMatrix().length; index++) {
                    org.bukkit.inventory.ItemStack item = event.getInventory().getMatrix()[index];
                    if(item == null) { continue; }
                    if(!matrix.containsKey(item.getType())) {
                        matrix.put(item.getType(), new TreeSet<>(new ItemStackComparator()));
                    }
                    matrix.get(item.getType()).add(new ItemStack(item));
                }

                // get items in recipe list
                Map<Material, TreeSet<ItemStack>> ingredients = new HashMap<>();
                String map = String.join("", recipe.getShape());
                for(char c : map.toCharArray()) {
                    RecipeChoice choice = recipe.getChoiceMap().get(c);
                    ItemStack itemchoice = null;
                    if(choice instanceof RecipeChoice.MaterialChoice materialchoice) {
                        itemchoice = new ItemStack(materialchoice.getItemStack());
                    } else if(choice instanceof RecipeChoice.ExactChoice exactchoice) {
                        itemchoice = new ItemStack(exactchoice.getItemStack());
                    }
                    if(itemchoice == null) { continue; }

                    if(!ingredients.containsKey(itemchoice.bukkitstack().getType())) {
                        ingredients.put(itemchoice.bukkitstack().getType(), new TreeSet<>(new ItemStackComparator()));
                    }
                    ingredients.get(itemchoice.bukkitstack().getType()).add(itemchoice);
                }

                // get the maximum amount of items that can be made
                int created = Integer.MAX_VALUE;
                List<ItemStack> remove = new ArrayList<>();
                for(Material material : ingredients.keySet()) {
                    while(!ingredients.get(material).isEmpty()) {
                        // calculate the amount created
                        ItemStack ingredient = ingredients.get(material).removeFirst();
                        ItemStack input = matrix.get(material).removeFirst();
                        created = Math.min(created, input.bukkitstack().getAmount() / ingredient.bukkitstack().getAmount());

                        // find item stack size to remove
                        ItemStack toremove = ingredient.clone();
                        toremove.bukkitstack().setAmount(ingredient.bukkitstack().getAmount() * created);
                        remove.add(toremove);
                    }
                }

                // remove items from matrix
                org.bukkit.inventory.ItemStack[] rawmatrix = event.getInventory().getMatrix();
                for(ItemStack toremove : remove) {
                    for(int i = 0; i < rawmatrix.length; i++) {
                        org.bukkit.inventory.ItemStack raw = toremove.bukkitstack();
                        if(rawmatrix[i] == null) { continue; }
                        if(raw.getType() == rawmatrix[i].getType()) {
                            if(raw.getAmount() < rawmatrix[i].getAmount()) {
                                rawmatrix[i].setAmount(rawmatrix[i].getAmount() - raw.getAmount());
                            } else if(raw.getAmount() == rawmatrix[i].getAmount()) {
                                rawmatrix[i] = null;
                            }
                        }
                    }
                }
                event.getInventory().setMatrix(rawmatrix);

                // give the player the items
                for(int item = 0; item < created; ++item) {
                    synchronized (player) { crafting = player.crafting().roll(); }
                    log(Level.INFO, String.format("%s crafting %s with %d craft roll", player.name(), craftable.displayname(), crafting));
                    player.bukkitplayer().getInventory().addItem(craftable.create(crafting).bukkitstack());
                }
            }

            // cancel original event since we handled the crafting in this case
            event.setCancelled(true);

        // Single Item Crafting
        } else {
            synchronized (player) { crafting = player.crafting().roll(); }
            log(Level.INFO, String.format("%s crafting %s with %d craft roll", player.name(), craftable.displayname(), crafting));
            event.setCurrentItem(craftable.create(crafting).bukkitstack());
        }
    }
}
