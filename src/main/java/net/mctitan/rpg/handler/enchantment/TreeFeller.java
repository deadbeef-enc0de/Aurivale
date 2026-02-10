package net.mctitan.rpg.handler.enchantment;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class TreeFeller implements Listener {
    private static final TreeFeller instance = new TreeFeller();
    private final Set<Location> skip = new HashSet<>();
    private final Set<Material> logs = Set.of(Material.ACACIA_LOG, Material.BIRCH_LOG, Material.CHERRY_LOG, Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG, Material.MANGROVE_LOG, Material.OAK_LOG, Material.PALE_OAK_LOG, Material.SPRUCE_LOG,
            Material.CRIMSON_STEM, Material.WARPED_STEM);

    private TreeFeller() {}

    public static TreeFeller instance() { return instance; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        // should we even do anything
        if(event.isCancelled() || !logs.contains(event.getBlock().getType())) {
            return;
        }

        // if this block break is from our own extra block destruction, skip it
        Location location = new Location(event.getBlock().getLocation());
        synchronized (skip) {
            if (skip.contains(location)) {
                skip.remove(location);
                return;
            }
        }

        // get some starting info
        Player player = DataManager.instance().player(event.getPlayer());
        ItemStack inhand = new ItemStack(event.getPlayer().getEquipment().getItemInMainHand());
        Block broken = event.getBlock();
        int feller = player.enchantments().level(Enchantment.tree_feller);

        // if not, do nothing else
        if(feller < 1) {
            return;
        }

        // we will be removing the block ourselves
        event.setCancelled(true);

        // start from the broken block and do a breadth first search fo
        LinkedList<Location> queue = new LinkedList<>();
        Set<Location> memo = new HashSet<>();
        Location start = new Location(broken.getLocation());
        queue.addLast(start);
        memo.add(start);

        // while there are blocks to process, go do
        while(!queue.isEmpty()) {
            // make sure the in hand item hasn't broken
            if(inhand == null) {
                break;
            }

            // get the next location
            Location loc = queue.removeFirst();

            for (Location adj : loc.diagonaladjacent()) {
                // check memoization and block type at location
                if (memo.contains(adj) || !logs.contains(adj.block().getType())) {
                    continue;
                }

                // add the location to the queue and memo
                queue.addLast(adj);
                memo.add(adj);
            }

            // keep track of then break the block
            synchronized (skip) { skip.add(loc); }
            boolean destroyed = event.getPlayer().breakBlock(loc.block());
            if (!destroyed) {
                // if not destroyed don't run the tool durability code
                continue;
            }

            // damage item in hand, if broken remove item
            if(inhand.adddamage()) {
                event.getPlayer().getEquipment().setItemInMainHand(null);
                inhand = null;
                break;
            }
        }
    }
}
