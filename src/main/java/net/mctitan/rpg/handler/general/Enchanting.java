package net.mctitan.rpg.handler.general;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enchanting.Enchanter;
import net.mctitan.rpg.enums.EnchantingState;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Level;

public class Enchanting implements Listener, Logger {
    private static final Enchanting instance = new Enchanting();

    private Enchanting() {}

    public static Enchanting instance() { return instance; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // if there is no clicked inventory, do nothing
        if(event.getClickedInventory() == null) {
            return;
        }

        // make sure it was a player that clicked
        if(!(event.getWhoClicked() instanceof org.bukkit.entity.Player bukkitplayer)) {
            return;
        }

        // get player, item on cursor, and item clicked
        Player player = DataManager.instance().player(bukkitplayer);
        ItemStack cursor = new ItemStack(event.getCursor());
        ItemStack current = new ItemStack(event.getClickedInventory(), event.getSlot());

        // get player enchanting state
        EnchantingState enchantingstate;
        synchronized (player) {
            enchantingstate = player.enchantingstate();
        }

        // make sure the inventory clicked was a player inventory
        if(event.getClickedInventory().getType() != InventoryType.PLAYER) {
            if(enchantingstate == EnchantingState.ENCHANTING) {
                reset(player);
            }
            return;
        }

        // start enchantment process using the right click pickup half action
        if(event.getClick() == ClickType.RIGHT &&
           event.getAction() == InventoryAction.PICKUP_HALF &&
           enchantingstate == EnchantingState.NOT_ENCHANTING &&
           current.enchantertype() != null) {
            // cancel the inventory action
            event.setCancelled(true);

            // lock player to do work on the player
            synchronized (player) {
                // set the item on cursor
                ItemStack duplicate = current.clone();
                duplicate.bukkitstack().setAmount(1);
                player.bukkitplayer().setItemOnCursor(duplicate.bukkitstack());

                // set player state to enchanting
                player.enchantingstate(EnchantingState.ENCHANTING);
            }
        }

        // enchant target item with enchanter if left click and swap or move to other action
        else if((event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) &&
                (event.getAction() == InventoryAction.SWAP_WITH_CURSOR ||
                 event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                 event.getAction() == InventoryAction.NOTHING) &&
                enchantingstate == EnchantingState.ENCHANTING &&
                cursor.enchantertype() != null &&
                ((!cursor.enchantertype().meta() && current.enchantable()) ||
                 (cursor.enchantertype().meta() && current.enchantertype() != null &&
                  current.enchantertype().modable() && current.enchantermod() == null))) {
            // cancel the inventory action
            event.setCancelled(true);

            // get the enchanter
            Enchanter enchanter = cursor.enchantertype().enchanter();

            // lock the player to do work on the player
            synchronized (player) {
                // unset player item, doesn't have to be set
                EquipmentChange.instance().unset(player, event.getSlot());

                //enchant the item
                int craftingvalue = current.get(Key.CRAFT_VALUE_KEY, PersistentDataType.INTEGER, 0);
                int enchantingvalue = player.enchanting().roll() + craftingvalue;
                if(current.craftable() != null) { enchantingvalue += current.craftable().enchantingbonus(); }
                log(Level.INFO, String.format("%s enchanting %s using %s with %d enchant roll",
                        player.name(),
                        PlainTextComponentSerializer.plainText().serialize(current.displayname()),
                        enchanter.type().string(),
                        enchantingvalue
                ));
                if(enchanter.enchant(player, cursor, current, enchantingvalue)) {
                    // remove enchanter from inventory if enchant happened
                    player.bukkitplayer().getInventory().removeItem(cursor.bukkitstack());
                } else {
                    // enchanter did not apply, we need to set the player item
                    EquipmentChange.instance().set(player, event.getSlot(), current);
                }

                // reset state if left click or items ran out
                if(event.getClick() == ClickType.LEFT || !player.bukkitplayer().getInventory().containsAtLeast(cursor.bukkitstack(), 1)) {
                    // reset player state
                    reset(player);
                }
            }
        }

        // fall through, if player is in enchanting state, rest
        else if(enchantingstate == EnchantingState.ENCHANTING) {
            // cancel the inventory action
            event.setCancelled(true);

            // reset player state
            synchronized (player) {
                reset(player);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if(!(event.getWhoClicked() instanceof org.bukkit.entity.Player bukkitplayer)) {
            return;
        }
        Player player = DataManager.instance().player(bukkitplayer);

        // if the player is enchanting, don't allow drags
        synchronized (player) {
            if(player.enchantingstate() == EnchantingState.ENCHANTING) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(!(event.getPlayer() instanceof org.bukkit.entity.Player bukkitplayer)) {
            return;
        }
        Player player = DataManager.instance().player(bukkitplayer);

        // reset player on inventory close
        synchronized (player) {
            reset(player);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = DataManager.instance().player(event.getPlayer());

        // if player enchanting don't allow drop and reset state
        synchronized (player) {
            if(player.enchantingstate() == EnchantingState.ENCHANTING) {
                // cancel event
                event.setCancelled(true);

                // fake item on cursor is given to player so remove it from the inventory
                player.bukkitplayer().getInventory().removeItem(event.getItemDrop().getItemStack());

                // reset player state
                reset(player);
            }
        }
    }

    private void reset(Player player) {
        if(player.enchantingstate() == EnchantingState.NOT_ENCHANTING) {
            return;
        }

        // remove temporary enchanter
        player.bukkitplayer().setItemOnCursor(null);

        // reset player state
        player.enchantingstate(EnchantingState.NOT_ENCHANTING);
    }
}
