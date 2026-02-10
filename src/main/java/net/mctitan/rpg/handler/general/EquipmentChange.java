package net.mctitan.rpg.handler.general;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.action.attack.AttackAction;
import net.mctitan.rpg.enums.ActionName;
import net.mctitan.rpg.enums.CraftableGroup;
import net.mctitan.rpg.enums.ItemSlot;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.event.PlayerStatsUpdateEvent;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.AttributeModifier;
import net.mctitan.rpg.monster.Monster;
import net.mctitan.rpg.monster.Monsters;
import net.mctitan.rpg.util.Logger;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

public class EquipmentChange implements Listener, Logger {
    private static final EquipmentChange instance = new EquipmentChange();
    public static final int OFFHAND_SLOT = 40;
    public static final int HELMET_SLOT = 39;
    public static final int CHESTPLATE_SLOT = 38;
    public static final int LEGGING_SLOT = 37;
    public static final int BOOT_SLOT = 36;

    private EquipmentChange() {}

    public static EquipmentChange instance() { return instance; }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // get the monster type for the spawned entity
        Monster monster = Monsters.instance().get(event.getEntityType());

        // if the monster has equipment
        if(monster.hasequipment()) {
            // create the entity object to set the equipment
            DataManager.instance().entity(event.getEntity());
        }
    }

    @EventHandler
    public void OnPlayerInventorySlotChange(PlayerInventorySlotChangeEvent event) {
        // get player
        int slot = event.getSlot();
        Player player = DataManager.instance().player(event.getPlayer());

        // get held slot
        synchronized (player) {
            // Get item slot
            ItemSlot itemslot = slot(player, event.getSlot());

            // if the slot is not an equipment slot do nothing
            if (itemslot == ItemSlot.NONE) {
                return;
            }

            // get item stacks
            ItemStack after = new ItemStack(player.bukkitplayer().getInventory(), slot, event.getNewItemStack());
            ItemStack before = player.equipment().item(itemslot);

            // get before stack and unset it if it exists
            change(player, itemslot, before, after);
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        // get player and item stacks
        int slot = event.getNewSlot();
        Player player = DataManager.instance().player(event.getPlayer());

        synchronized (player) {
            ItemStack before = player.equipment().item(ItemSlot.MAIN_HAND);
            ItemStack after = new ItemStack(player.bukkitplayer().getInventory(), slot);

            change(player, ItemSlot.MAIN_HAND, before, after);
        }
    }

    private void change(Player player, ItemSlot itemslot, ItemStack before, ItemStack after) {
        if (before != null) {
            // reset base attack speed to 1/tick
            player.bukkitplayer().getAttribute(Attribute.ATTACK_SPEED).setBaseValue(20);

            // if old item is a BOW or CROSSBOW and the offhand is a SHIELD, set shield modifiers
            if(before.craftable() != null &&
                    (before.craftable().group() == CraftableGroup.BOW || before.craftable().group() == CraftableGroup.CROSSBOW) &&
                    player.equipment().item(ItemSlot.OFF_HAND) != null &&
                    player.equipment().item(ItemSlot.OFF_HAND).craftable().group() == CraftableGroup.SHIELD) {
                // add offhand shield
                set(player, player.equipment().item(ItemSlot.OFF_HAND));
            }

            // set the equipment slot to null on the entity
            player.equipment().item(itemslot, null);

            // unset modifiers from item that was equipped
            unset(player, before);
        }

        // check to see if after stack can be equipped in this slot
        if (after.craftable() != null && after.craftable().itemslot() == itemslot) {
            // set base attack speed to 0 if the item is a melee weapon
            if(after.craftable().group().melee() || hasattackspeed(after)) {
                player.bukkitplayer().getAttribute(Attribute.ATTACK_SPEED).setBaseValue(0);
            }

            // if the new item is a BOW or CROSSBOW and the offhand is a SHIELD, unset shield modifiers
            if((after.craftable().group() == CraftableGroup.BOW || after.craftable().group() == CraftableGroup.CROSSBOW) &&
                    player.equipment().item(ItemSlot.OFF_HAND) != null &&
                    player.equipment().item(ItemSlot.OFF_HAND).craftable().group() == CraftableGroup.SHIELD) {
                // remove offhand shield
                unset(player, player.equipment().item(ItemSlot.OFF_HAND));
            }

            // set the equipment slot
            player.equipment().item(itemslot, after);

            // if new items is a SHIELD and a BOW or CROSSBOW isn't equipped set the modifiers
            if(after.craftable().group() != CraftableGroup.SHIELD || (
                    after.craftable().group() == CraftableGroup.SHIELD &&
                    (player.equipment().item(ItemSlot.MAIN_HAND) == null ||
                     (player.equipment().item(ItemSlot.MAIN_HAND).craftable().group() != CraftableGroup.BOW &&
                      player.equipment().item(ItemSlot.MAIN_HAND).craftable().group() != CraftableGroup.CROSSBOW)))) {
                // set modifiers from the new item
                set(player, after);
            }
        }

        // if the materials are different reset the default attack timer
        if(before == null || before.bukkitstack() == null || after.bukkitstack() == null ||
           before.bukkitstack().getType() != after.bukkitstack().getType()) {
            AttackAction action = (AttackAction)player.actions().action(ActionName.DEFAULT);
            action.attacktime().reset(player);
        }

        // send out that player stat scoreboard should change
        PlayerStatsUpdateEvent event = new PlayerStatsUpdateEvent(player);
        event.callEvent();
    }

    private boolean hasattackspeed(ItemStack stack) {
        for(Modifier modifier : stack.modifiers()) {
            if(!(modifier instanceof AttributeModifier attributemod) ||
                    attributemod.attribute() != net.mctitan.rpg.enums.Attribute.ATTACK_SPEED ||
                    attributemod.operator() != Operator.FLAT) {
                continue;
            }

            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = DataManager.instance().player(event.getPlayer());
        synchronized (player) {
            unset(player);
        }
    }

    private ItemSlot slot(Player player, int slot) {
        ItemSlot itemslot = ItemSlot.NONE;
        if(slot == player.bukkitplayer().getInventory().getHeldItemSlot()) { itemslot = ItemSlot.MAIN_HAND; }
        else if(slot == OFFHAND_SLOT) { itemslot = ItemSlot.OFF_HAND; }
        else if(slot == HELMET_SLOT) { itemslot = ItemSlot.HEAD; }
        else if(slot == CHESTPLATE_SLOT) { itemslot = ItemSlot.CHEST; }
        else if(slot == LEGGING_SLOT) { itemslot = ItemSlot.LEGS; }
        else if(slot == BOOT_SLOT) { itemslot = ItemSlot.FEET; }

        return itemslot;
    }

    public void unset(Player player) {
        ItemSlot[] itemslots = {
                ItemSlot.MAIN_HAND,
                ItemSlot.OFF_HAND,
                ItemSlot.HEAD,
                ItemSlot.CHEST,
                ItemSlot.LEGS,
                ItemSlot.FEET
        };

        for(ItemSlot itemslot : itemslots) {
            ItemStack before = player.equipment().item(itemslot);
            if(before != null) {
                unset(player, before);
            }
        }
    }

    public void unset(Player player, int slot) {
        synchronized (player) {
            ItemSlot itemslot = slot(player, slot);
            if(itemslot == ItemSlot.NONE || player.equipment().item(itemslot) == null) {
                return;
            }

            ItemStack item = new ItemStack(player.bukkitplayer().getInventory(), slot);
            unset(player, item);
            player.equipment().item(itemslot, null);
        }
    }

    public void set(Player player, int slot, ItemStack item) {
        synchronized (player) {
            ItemSlot itemslot = slot(player, slot);
            if(itemslot == ItemSlot.NONE) {
                return;
            }

            set(player, item);
            player.equipment().item(itemslot, item);
        }
    }

    private void unset(Entity entity, ItemStack item) {
        for (Modifier modifier : item.modifiers()) {
            log(Level.INFO, String.format("Removing from %s modifier \"%s\"",
                    entity.name(), PlainTextComponentSerializer.plainText().serialize(modifier.text())));
            modifier.unapply(entity);
        }
    }

    private void set(Entity entity, ItemStack item) {
        for (Modifier modifier : item.modifiers()) {
            log(Level.INFO, String.format("Adding to %s modifier \"%s\"",
                    entity.name(), PlainTextComponentSerializer.plainText().serialize(modifier.text())));
            modifier.apply(entity);
        }
    }
}
