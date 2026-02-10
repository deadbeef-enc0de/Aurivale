package net.mctitan.rpg.handler.general;

import net.mctitan.rpg.brewing.BrewingRecipes;
import net.mctitan.rpg.brewing.BrewingStands;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.brewing.BrewingStand;
import net.mctitan.rpg.enums.BrewingStandSlot;
import net.mctitan.rpg.util.directional.ChunkLocation;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.BlockInventoryHolder;

import java.util.List;

public class Brewing implements Listener {
    private static final Brewing instance = new Brewing();

    private Brewing() {}

    public static Brewing instance() { return instance; }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        // only care about brewing inventories
        if(event.getInventory().getType() != InventoryType.BREWING) {
            return;
        }

        // create brewing stand if it doesn't exist
        org.bukkit.block.BrewingStand bukkitstand = (org.bukkit.block.BrewingStand)event.getInventory().getHolder();
        Location blockloc = new Location(bukkitstand.getLocation());
        if(BrewingStands.instance().stand(blockloc) == null) {
            BrewingStands.instance().create(blockloc);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // only check for placed brewing stands
        if(event.getBlock().getType() != Material.BREWING_STAND) {
            return;
        }

        // create brewing stand on placement
        Location blockloc = new Location(event.getBlock().getLocation());
        BrewingStands.instance().create(blockloc);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // only check for placed brewing stands
        if(event.getBlock().getType() != Material.BREWING_STAND) {
            return;
        }

        // remove brewing stand on breakage
        Location blockloc = new Location(event.getBlock().getLocation());
        BrewingStands.instance().remove(blockloc);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        ChunkLocation chunklocation = new ChunkLocation(event.getChunk());
        BrewingStands.instance().chunkload(chunklocation);
    }

    @EventHandler
    public void unChunkUnload(ChunkUnloadEvent event) {
        ChunkLocation chunklocation = new ChunkLocation(event.getChunk());
        BrewingStands.instance().chunkunload(chunklocation);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // we only care about inventory slots in brewing stands
        if(event.getInventory().getType() != InventoryType.BREWING) {
            return;
        }
        BlockInventoryHolder holder = (BlockInventoryHolder)event.getInventory().getHolder();

        // get the raw slot
        BrewingStandSlot slot = BrewingStandSlot.rawslot(event.getRawSlot());

        // if it's a non shift click player or other slot allow it
        if((!event.isShiftClick() && slot == BrewingStandSlot.PLAYER) ||
            slot == BrewingStandSlot.OTHER ||
            (event.isShiftClick() && slot != BrewingStandSlot.PLAYER)) {
            return;
        }

        // cancel all events as we will handle things here ourselves
        event.setCancelled(true);

        // only allow regular left and right clicks
        if(event.getClick() != ClickType.LEFT && event.getClick() != ClickType.SHIFT_LEFT &&
           event.getClick() != ClickType.RIGHT && event.getClick() != ClickType.SHIFT_RIGHT) {
            return;
        }
        boolean left = event.getClick() == ClickType.LEFT ||  event.getClick() == ClickType.SHIFT_LEFT;
        boolean shift = event.isShiftClick();

        // get the player
        Player player = DataManager.instance().player(event.getWhoClicked().getUniqueId());

        // get item in hand and clicked
        ItemStack inhand = new ItemStack(event.getCursor());
        ItemStack inslot = new ItemStack(event.getCurrentItem());

        // if it's a shift click try to find a slot to put it into
        if(shift) {
            // figure out what slot the item clicked should go to
            List<BrewingStandSlot> slots = BrewingRecipes.instance().findslot(inslot);
            if(slots.size() == 1 && slots.getFirst() == BrewingStandSlot.OTHER) {
                // there is no brewing slot to use, bail
                return;
            }

            // change slot to single slot
            if(slots.size() == 1) {
                slot = slots.getFirst();
            }
            // if there are multiple slots, find one that is empty
            else {
                slot = BrewingStandSlot.OTHER;
                for (BrewingStandSlot potionslot : slots) {
                    if(holder.getInventory().getItem(potionslot.index()) == null) {
                        slot = potionslot;
                        break;
                    }
                }

                // if we still have the other slot, bail
                if(slot == BrewingStandSlot.OTHER) {
                    return;
                }
            }

            // move inslot to inhand and target slot to inslot
            inhand = inslot;
            org.bukkit.inventory.ItemStack bukkittarget = holder.getInventory().getItem(slot.index());
            if(bukkittarget == null) { bukkittarget = new org.bukkit.inventory.ItemStack(Material.AIR, 0); }
            inslot = new ItemStack(bukkittarget);

            // turn all shift right clicks to left clicks
            left = true;
        }

        // make sure the item in hand can be put into the slot clicked
        if(!inhand.bukkitstack().getType().isAir() && !BrewingRecipes.instance().checkslot(inhand, slot)) {
            // if the item can't go into that slot, bail
            return;
        }

        // exchange items between inhand and in slot
        if((left && inhand.bukkitstack().getType().isAir()) ||
           (left && inslot.bukkitstack().getType().isAir()) ||
           (!inhand.bukkitstack().getType().isAir() &&
            !inslot.bukkitstack().getType().isAir() &&
            !inhand.bukkitstack().asOne().equals(inslot.bukkitstack().asOne()))
        ) {
            holder.getInventory().setItem(slot.index(), inhand.bukkitstack());
            if(shift) {
                event.getClickedInventory().setItem(event.getSlot(), inslot.bukkitstack());
            } else {
                player.bukkitplayer().setItemOnCursor(inslot.bukkitstack());
            }
        }
        // move half the stack to the players hand
        else if(!left && inhand.bukkitstack().getType().isAir() && !inslot.bukkitstack().getType().isAir()) {
            int inslotamount = inslot.bukkitstack().getAmount() / 2;
            int inhandamount = inslot.bukkitstack().getAmount() - inslotamount;
            holder.getInventory().setItem(slot.index(), inslot.bukkitstack().asQuantity(inslotamount));
            if(shift) {
                event.getClickedInventory().setItem(event.getSlot(), inslot.bukkitstack().asQuantity(inhandamount));
            } else {
                player.bukkitplayer().setItemOnCursor(inslot.bukkitstack().asQuantity(inhandamount));
            }
        }
        // move one of the stack to the slot
        else if(!left && !inhand.bukkitstack().getType().isAir() && inslot.bukkitstack().getType().isAir()) {
            holder.getInventory().setItem(slot.index(), inhand.bukkitstack().asOne());
            if(shift) {
                event.getClickedInventory().setItem(event.getSlot(), inhand.bukkitstack().asQuantity(inhand.bukkitstack().getAmount() - 1));
            } else {
                player.bukkitplayer().setItemOnCursor(inhand.bukkitstack().asQuantity(inhand.bukkitstack().getAmount() - 1));
            }
        }
        // try to add entire stack to slot
        else if(left && !inhand.bukkitstack().getType().isAir() && !inslot.bukkitstack().getType().isAir() &&
                inhand.bukkitstack().asOne().equals(inslot.bukkitstack().asOne())) {
            int toadd = Math.min(inslot.bukkitstack().getType().getMaxStackSize() - inslot.bukkitstack().getAmount(),
                                 inhand.bukkitstack().getAmount());
            holder.getInventory().setItem(slot.index(), inslot.bukkitstack().asQuantity(inslot.bukkitstack().getAmount() + toadd));
            if(shift) {
                event.getClickedInventory().setItem(event.getSlot(), inhand.bukkitstack().asQuantity(inhand.bukkitstack().getAmount() - toadd));
            } else {
                player.bukkitplayer().setItemOnCursor(inhand.bukkitstack().asQuantity(inhand.bukkitstack().getAmount() - toadd));
            }
        }
        // try to add one of stack to slot
        else if(!left && !inhand.bukkitstack().getType().isAir() && !inslot.bukkitstack().getType().isAir() &&
                inhand.bukkitstack().asOne().equals(inslot.bukkitstack().asOne()) &&
                inslot.bukkitstack().getAmount() < inslot.bukkitstack().getType().getMaxStackSize()) {
            holder.getInventory().setItem(slot.index(), inslot.bukkitstack().asQuantity(inslot.bukkitstack().getAmount() + 1));
            if(shift) {
                event.getClickedInventory().setItem(event.getSlot(), inhand.bukkitstack().asQuantity(inhand.bukkitstack().getAmount() - 1));
            } else {
                player.bukkitplayer().setItemOnCursor(inhand.bukkitstack().asQuantity(inhand.bukkitstack().getAmount() - 1));
            }
        }

        // get the brewing stand, check fuel, check for recipe
        BrewingStand stand = BrewingStands.instance().stand(holder.getBlock().getLocation());
        stand.checkfuel();
        stand.checkrecipe(player);
    }
}
