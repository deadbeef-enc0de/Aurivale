package net.mctitan.rpg.handler.general;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.CraftableGroup;
import net.mctitan.rpg.spell.Spells;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpellCasting implements Listener {
    private static final SpellCasting instance = new SpellCasting();

    private SpellCasting() {}

    public static SpellCasting instance() { return instance; }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(!event.getAction().isLeftClick()) {
            return;
        }

        // item in hand must be a wand
        Player caster = DataManager.instance().player(event.getPlayer());
        ItemStack stack = new ItemStack(caster.bukkitplayer().getInventory().getItemInMainHand());
        if(stack.craftable() == null || stack.craftable().group() != CraftableGroup.WAND) {
            return;
        }

        // cast spells
        Spells.instance().cast(caster, stack);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // we only care about players
        if(event.getDamager().getType() != EntityType.PLAYER) {
            return;
        }

        // item in hand must be a wand
        Player caster = DataManager.instance().player(event.getDamager().getUniqueId());
        ItemStack stack = new ItemStack(caster.bukkitplayer().getInventory().getItemInMainHand());
        if(stack.craftable() == null || stack.craftable().group() != CraftableGroup.WAND) {
            return;
        }

        // cast spells
        Spells.instance().cast(caster, stack);

        // cancel vanilla event
        event.setCancelled(true);
    }
}
