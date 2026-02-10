package net.mctitan.rpg.handler.general;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Party;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.LoreSlot;
import net.mctitan.rpg.util.Item;
import net.mctitan.rpg.util.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public class Parties implements Listener {
    private static final Parties instance = new Parties();
    private final ItemStack invite;
    private final ItemStack leave;

    private Parties() {
        // setup item stack for invites
        invite = Item.cleanstack(Material.PAPER);
        invite.set(Key.PARTY_INVITE_KEY, true);
        ItemMeta meta = invite.bukkitstack().getItemMeta();
        CustomModelDataComponent customModel = meta.getCustomModelDataComponent();
        customModel.setFloats(List.of(20001f));
        meta.setCustomModelDataComponent(customModel);
        invite.bukkitstack().setItemMeta(meta);
        invite.displayname(Component.text("Party Invite").color(NamedTextColor.GOLD));
        invite.addlore(LoreSlot.FOOTER, MiniMessage.miniMessage().deserialize("<!italic><dark_gray>(Right click with this in your hand"));
        invite.addlore(LoreSlot.FOOTER, MiniMessage.miniMessage().deserialize("<!italic><dark_gray>to join the player's party.)"));

        // setup item stack for leaving party
        leave = Item.cleanstack(Material.PAPER);
        leave.set(Key.PARTY_LEAVE_KEY, true);
        meta = leave.bukkitstack().getItemMeta();
        customModel = meta.getCustomModelDataComponent();
        customModel.setFloats(List.of(20002f));
        meta.setCustomModelDataComponent(customModel);
        leave.bukkitstack().setItemMeta(meta);
        leave.displayname(Component.text("Leave Party").color(NamedTextColor.RED));
        leave.addlore(LoreSlot.FOOTER, MiniMessage.miniMessage().deserialize("<!italic><dark_gray>(Right click with this in your hand"));
        leave.addlore(LoreSlot.FOOTER, MiniMessage.miniMessage().deserialize("<!italic><dark_gray>to leave your party.)"));
    }

    public static Parties instance() { return instance; }
    public static void initialize() { instance.load(); }

    private void load() {
        // add party invites recipe
        ItemStack invites = new ItemStack(invite().bukkitstack().asQuantity(5));
        NamespacedKey key = new NamespacedKey(Aurivale.instance(), "party_invites");
        ShapedRecipe partyinvites = new ShapedRecipe(key, invites.bukkitstack());
        partyinvites.shape(" p ","pip"," p ");
        partyinvites.setIngredient('p', Material.PAPER);
        partyinvites.setIngredient('i', Material.IRON_INGOT);
        Aurivale.instance().getServer().addRecipe(partyinvites);

        // add leave invite recipe
        ItemStack leave = new ItemStack(leave().bukkitstack().asQuantity(1));
        key = new NamespacedKey(Aurivale.instance(), "party_leave");
        ShapedRecipe partyleave = new ShapedRecipe(key, leave.bukkitstack());
        partyleave.shape("p");
        partyleave.setIngredient('p', Material.PAPER);
        Aurivale.instance().getServer().addRecipe(partyleave);
    }

    public ItemStack invite() { return invite.clone(); }
    public ItemStack leave() { return leave.clone(); }

    @EventHandler
    public void onInviteCraft(CraftItemEvent event) {
        // only run checks for player invites
        ItemStack current = new ItemStack(event.getCurrentItem());
        if(!current.bukkitstack().asOne().equals(invite().bukkitstack())) {
            return;
        }

        // don't allow shift or right click crafting for this item
        if(event.isShiftClick() || event.isRightClick()) {
            event.setCancelled(true);
        }

        // get player
        Player player = DataManager.instance().player((org.bukkit.entity.Player) event.getWhoClicked());

        // if player is in party, remove them from it
        if(player.party() != null && player.party().owner() != player) {
            player.party().removemember(player);
            DataManager.instance().save(player.party());
            player.party(null);
        }
        // if player owns a party, destroy it
        else if(player.party() != null && player.party().owner() == player) {
            DataManager.instance().remove(player.party().uuid);
        }

        // create party
        Party party = DataManager.instance().party(player);

        // modify the result
        current.addlore(LoreSlot.HEADER, MiniMessage.miniMessage().deserialize(String.format(
                "<!italic><green>%s's Invite",
                player.name()
        )));
        current.uuid(party.uuid.uuid());
        event.setCurrentItem(current.bukkitstack());
    }

    @EventHandler
    public void onLeaveCraft(CraftItemEvent event) {
        // only run checks for party leave
        ItemStack current = new ItemStack(event.getCurrentItem());
        if(!current.bukkitstack().asOne().equals(leave().bukkitstack())) {
            return;
        }

        // don't allow shift or right click crafting for this item
        if(event.isShiftClick() || event.isRightClick()) {
            event.setCancelled(true);
        }

        // get player
        Player player = DataManager.instance().player((org.bukkit.entity.Player) event.getWhoClicked());

        // if player is not in party, cancel and bail
        if(player.party() == null) {
            event.setCancelled(true);
            return;
        }

        // player is not leader, just remove that player
        if(player.party().owner() != player) {
            player.party().removemember(player);
            DataManager.instance().save(player.party());
            player.party(null);
        }

        // player is leader, disband the party
        else {
            DataManager.instance().remove(player.party().uuid);
        }

        // set current item to nothing as to not actually give an item
        event.setCurrentItem(new org.bukkit.inventory.ItemStack(Material.AIR));
    }

    @EventHandler
    public void onPlayerInviteAccept(PlayerInteractEvent event) {
        // make sure interaction is a right click
        if(!event.getAction().isRightClick()) {
            return;
        }

        // get player that did interaction
        Player player = DataManager.instance().player(event.getPlayer());

        // make sure item stack is a party invite
        ItemStack inhand = new ItemStack(player.bukkitplayer().getInventory().getItemInMainHand());
        if(inhand.bukkitstack() == null || !inhand.has(Key.PARTY_INVITE_KEY) || !inhand.get(Key.PARTY_INVITE_KEY)) {
            return;
        }

        // make sure the party still exists
        Party party = DataManager.instance().party(inhand.uuid());
        if(party == null) {
            // remove invalid party invite stack
            player.bukkitplayer().getInventory().setItemInMainHand(null);
            return;
        }

        // make sure player isn't a member already
        if(party.member(player) || party.owner() == player) {
            return;
        }

        // if the player is in party but no leader
        if(player.party() != null && player.party().owner() != player) {
            // remove player from old party
            player.party().removemember(player);
            DataManager.instance().save(player.party());
            player.party(null);
        }
        // if player is in party but is leader
        else if(player.party() != null && player.party().owner() == player) {
            // destroy old party
            DataManager.instance().remove(player.party().uuid);
        }

        // join party in the invite
        party.addmember(player);
        player.party(party);
        DataManager.instance().save(party);
        DataManager.instance().save(player);

        // remove one of item in hand
        if(inhand.bukkitstack().getAmount() == 1) {
            player.bukkitplayer().getInventory().setItemInMainHand(null);
        } else {
            inhand = new ItemStack(inhand.bukkitstack().asQuantity(inhand.bukkitstack().getAmount() - 1));
            player.bukkitplayer().getInventory().setItemInMainHand(inhand.bukkitstack());
        }
    }
}
