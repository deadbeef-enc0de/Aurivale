package net.mctitan.rpg.handler.enchantment;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;

import java.util.HashMap;
import java.util.Iterator;

public class ItemBag implements Listener {
    private static final ItemBag instance = new ItemBag();

    private ItemBag() {}

    public static ItemBag instance() { return instance; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDropItem(BlockDropItemEvent event) {
        // should we even do anything
        if(event.isCancelled()) {
            return;
        }

        // get some intial info
        Player player = DataManager.instance().player(event.getPlayer());
        ItemStack inhand = new ItemStack(event.getPlayer().getEquipment().getItemInMainHand());
        Location location = new Location(event.getBlock().getLocation());
        int itembag = player.enchantments().level(Enchantment.item_bag);

        // make sure there is an item bag level
        if(itembag < 1) {
            return;
        }

        Iterator<Item> iterator = event.getItems().iterator();
        while(iterator.hasNext()) {
            Item item = iterator.next();

            // remove drop as we will drop what is left
            iterator.remove();

            // call functionality fo bagging or dropping item
            bagitems(player, location, item.getItemStack());
        }
    }

    public static void bagitems(Player player, Location location, org.bukkit.inventory.ItemStack drop) {
        // try to add the item to the inventory
        HashMap<Integer, org.bukkit.inventory.ItemStack> ret = player.bukkitplayer().getInventory().addItem(drop);

        // drop what doesn't fit
        for(org.bukkit.inventory.ItemStack stack : ret.values()) {
            location.world().dropItemNaturally(location.center().bukkit(), stack);
        }
    }
}
