package net.mctitan.rpg.handler.general;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class ItemCooldown implements Listener {
    private static final ItemCooldown instance = new ItemCooldown();

    private ItemCooldown() {}

    public static ItemCooldown instance() { return instance; }

    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        // get player and itemstacks
        Player player = DataManager.instance().player(event.getPlayer());
        ItemStack prevstack = new ItemStack(event.getPlayer().getInventory().getItem(event.getPreviousSlot()));
        ItemStack newstack = new ItemStack(event.getPlayer().getInventory().getItem(event.getNewSlot()));

        // set cooldown
        unsetcooldown(player, prevstack);
        setcoolddown(player, newstack);
    }

    @EventHandler
    public void OnPlayerInventorySlotChange(PlayerInventorySlotChangeEvent event) {
        // only update cooldown if the slot changed is the item in main hand
        if(event.getSlot() != event.getPlayer().getInventory().getHeldItemSlot()) {
            return;
        }

        // get player and itemstacks
        Player player = DataManager.instance().player(event.getPlayer());
        ItemStack prevstack = new ItemStack(event.getOldItemStack());
        ItemStack newstack = new ItemStack(event.getNewItemStack());

        // set cooldown
        unsetcooldown(player, prevstack);
        setcoolddown(player, newstack);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.getPlayer().getScheduler().runDelayed(Aurivale.instance(), task -> {
            // get player and item in hand
            Player player = DataManager.instance().player(event.getPlayer());
            ItemStack stack = new ItemStack(event.getPlayer().getInventory().getItemInMainHand());

            // set cooldown for item
            setcoolddown(player, stack);
            }, null, 1);
    }

    public void unsetcooldown(Player player, ItemStack stack) {
        // make sure the bukkit stack exists
        if(stack.bukkitstack() == null) {
            return;
        }

        // unset cooldown for that item type
        player.bukkitplayer().setCooldown(stack.bukkitstack().getType(), 0);
    }

    public void setcoolddown(Player player, ItemStack stack) {
        // make sure the bukkit stack exists
        if(stack.bukkitstack() == null) {
            return;
        }

        // get cooldown
        int cooldown = 0;
        if(stack.bukkitstack() != null && stack.craftable() != null) {
            cooldown = player.cooldown().get(stack);
        }

        // set cooldown for stack
        player.bukkitplayer().setCooldown(stack.bukkitstack().getType(), cooldown);
    }
}
