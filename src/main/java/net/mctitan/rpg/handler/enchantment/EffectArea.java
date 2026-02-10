package net.mctitan.rpg.handler.enchantment;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.CraftableGroup;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class EffectArea implements Listener {
    private static final EffectArea instance = new EffectArea();
    private static final Map<Material,Material> tilling = Map.of(
            Material.DIRT, Material.FARMLAND,
            Material.GRASS_BLOCK, Material.FARMLAND,
            Material.DIRT_PATH, Material.FARMLAND,
            Material.COARSE_DIRT, Material.DIRT
    );

    private Set<Location> skip = new HashSet<>();
    private Map<UUID, BlockFace> lastface = new HashMap<>();

    private EffectArea() {}

    public static EffectArea instance() { return instance; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        // check to see if the event is cancelled
        if(event.isCancelled()) {
            return;
        }

        // don't look at block breaks from this event handling being fired
        Location location = new Location(event.getBlock().getLocation());
        synchronized (skip) {
            if (skip.contains(location)) {
                skip.remove(location);
                return;
            }
        }

        // get the item in hand, area effect level, and unbreaking level
        Player player = DataManager.instance().player(event.getPlayer());
        ItemStack inhand = new ItemStack(event.getPlayer().getInventory().getItemInMainHand());
        int area = player.enchantments().level(Enchantment.effect_area);

        // make sure item used is the preferred tool for breaking the block
        if(inhand.bukkitstack() == null || !event.getBlock().isPreferredTool(inhand.bukkitstack())) {
            return;
        }

        // if there is no are level, do nothing
        if(area < 1) {
            return;
        }

        // get block face the break happened on
        BlockFace face = null;
        synchronized (lastface) {
            face = lastface.get(event.getPlayer().getUniqueId());
        }
        if(face == null) {
            return;
        }

        // get short and long faces for block removal
        BlockFace ldir = null;
        BlockFace sdir = null;
        if(face != BlockFace.UP && face != BlockFace.DOWN) {
            sdir = BlockFace.UP;
            if(face == BlockFace.EAST || face == BlockFace.WEST) {
                ldir = BlockFace.NORTH;
            } else {
                ldir = BlockFace.EAST;
            }
        } else {
            double direction = event.getPlayer().getLocation().getYaw();
            if((direction >= -45 && direction < 45) || direction >= 135 || direction < -135) {
                ldir = BlockFace.EAST;
                sdir = BlockFace.NORTH;
            } else {
                ldir = BlockFace.NORTH;
                sdir = BlockFace.EAST;
            }
        }

        // set event to cancelled as we are handling block removal ourselves
        event.setCancelled(true);

        // go through area and break extra blocks
        for(int l = -2; l <= 2; ++l) {
            for(int s = -1; s <= 1; ++s) {
                // make sure the location is breakable for the area level
                if(Math.abs(l) == 2 && area < 3) { continue; }
                if((Math.abs(l) + Math.abs(s)) == 2 && area < 2) { continue; }

                // get the block to break
                Block block = event.getBlock().getRelative(ldir, l).getRelative(sdir, s);

                // make sure item in hand is the right one for the block
                if(inhand == null || inhand.bukkitstack() == null || !block.isPreferredTool(inhand.bukkitstack())) {
                    continue;
                }

                // don't allow certain things to be broken
                if(block.getType().isAir() ||
                        block.getType() == Material.WATER ||
                        block.getType() == Material.LAVA ||
                        block.getType() == Material.BEDROCK ||
                        block.getType() == Material.END_PORTAL_FRAME) {
                    continue;
                }

                // keep track of then break the block
                synchronized (skip) {
                    skip.add(new Location(block.getLocation()));
                }

                // try to destroy block
                boolean destroyed = event.getPlayer().breakBlock(block);
                if(!destroyed) {
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

    @EventHandler
    public void lastFaceHit(PlayerInteractEvent event) {
        // make sure the action we care about isn't cancelled
        if(event.useItemInHand() == Result.DENY) {
            return;
        }

        // make sure it's a right click on a block
        if(event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        synchronized (lastface) {
            lastface.put(event.getPlayer().getUniqueId(), event.getBlockFace());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hoeArea(PlayerInteractEvent event) {
        // make sure the action we care about isn't cancelled
        if(event.useItemInHand() == Result.DENY) {
            return;
        }

        // make sure it's a right click on a block
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // make sure the block can be tilled
        if(!tilling.containsKey(event.getClickedBlock().getType())) {
            return;
        }

        // get some intial info
        ItemStack inhand = new ItemStack(event.getPlayer().getEquipment().getItemInMainHand());
        int arealevel = inhand.enchantment(Enchantment.effect_area);

        // make sure item is a hoe
        if(inhand.craftable() == null || inhand.craftable().group() != CraftableGroup.HOE) {
            return;
        }

        // if there is no area, move on
        if(arealevel < 1) {
            return;
        }

        // we will be changing the block ourselves
        event.setCancelled(true);

        // get till result
        Material till = tilling.get(event.getClickedBlock().getType());

        // go through nearby blocks and till the land
        for(int x = -(arealevel + 1); x <= (arealevel + 1); ++x) {
            for (int z = -(arealevel + 1); z <= (arealevel + 1); ++z) {
                // make sure the tool still exists
                if (inhand == null || inhand.bukkitstack() == null) {
                    continue;
                }

                // get the block to check
                Block block = event.getClickedBlock().getRelative(x, 0, z);

                // make sure block above is air
                if (!block.getRelative(BlockFace.UP).getType().isAir()) {
                    continue;
                }

                // make sure the till output is the same as the sourfce block
                if (tilling.get(block.getType()) != till) {
                    continue;
                }

                // set the block type
                block.setType(till);

                // damage item in hand, if broken remove item
                if(inhand.adddamage()) {
                    event.getPlayer().getEquipment().setItemInMainHand(null);
                    inhand = null;
                    break;
                }
            }
        }
    }
}
