package net.mctitan.rpg.handler.general;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.crafting.Craftables;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.damage.DamageInstance;
import net.mctitan.rpg.enums.SpellActivation;
import net.mctitan.rpg.loot.LootDrops;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.Text;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.LootGenerateEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class RandomLoot implements Listener, Logger {
    private static final RandomLoot instance = new RandomLoot();
    private final Map<Player, Location> locations = new HashMap<>();
    private final Map<Location, Integer> locationluck = new HashMap<>();
    private final int basedropcount;
    private final double basedropchance;

    private RandomLoot() {
        Configuration config = Aurivale.instance().getConfig("loot");
        this.basedropcount = config.getInt("base_item_drop_count", 0);
        this.basedropchance = config.getDouble("drop_chance", 0.5);
    }

    public static RandomLoot instance() { return instance; }

    public int itemcount(boolean passive, int luck) {
        int items = passive ? 0 : basedropcount;
        double dropluck = luck / 200d;
        double dropchance;
        if(dropluck > 0) {
            dropchance = -(1/(dropluck+1))*(1-basedropchance) + 1;
        } else {
            dropchance = Math.pow(1 / basedropchance, dropluck - 1);
        }
        while(Math.nextDouble() < dropchance) { ++items; }

        return items;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // get entity
        Entity entity = DataManager.instance().entity(event.getEntity());

        // check for no loot or if the death was a player
        if(entity == null || entity.noloot() || entity instanceof Player) {
            return;
        }

        // only entities that have been damage by players can drop loot
        DamageInstance instance = entity.lastdamage().mostrecent();
        if(instance == null) {
            return;
        }

        // activate on kill spells for the killer
        entity.lastdamage().mostrecent().player().actions().spell().activate(SpellActivation.ON_KILL);

        // setup some needed variables
        int luck = (int)entity.lastdamage().luck();
        int items = itemcount(entity.monster().passive(), luck);
        log(Level.INFO, String.format("%s killed dropping %d items luck=%d",
                entity.name(),
                items,
                luck
        ));

        // drop the extra items
        List<ItemStack> drops = LootDrops.instance().drops(entity, luck, items);
        event.getDrops().addAll(drops.stream().map(ItemStack::bukkitstack).toList());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        // filter out spawn reasons we don't care about
        if(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BREEDING &&
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM &&
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN &&
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.DISPENSE_EGG &&
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.EGG &&
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SLIME_SPLIT &&
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER &&
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            return;
        }

        // get entity and set no loot flag
        Entity entity = DataManager.instance().entity(event.getEntity());
        entity.noloot(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        // get the player and location clicked
        Player player = DataManager.instance().player(event.getPlayer());
        Location location = new Location(event.getClickedBlock().getLocation());

        // remove players previous clicked location
        if(locations.containsKey(player)) {
            locationluck.remove(locations.get(player));
        }

        // track player, location, and luck
        locations.put(player, location);
        locationluck.put(location, player.dropluck().dropluck());
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        List<ItemStack> generated = event.getLoot().stream().map(ItemStack::new).toList();
        List<ItemStack> loot = new LinkedList<>();
        for(ItemStack genstack : generated) {
            // check item type and filter certain ones out
            if(genstack.bukkitstack().getType() == Material.EXPERIENCE_BOTTLE ||
               genstack.bukkitstack().getType() == Material.ENCHANTED_BOOK) {
               continue;
            }

            // get craftable name from item type
            String craftablename = Text.enumtocraftable(genstack.bukkitstack().getType().name());

            // if it's an axe, 50/50 chance for battleaxe
            if(craftablename.contains("axe") && !craftablename.contains("pickaxe") && Math.nextDouble() < 0.5) {
                craftablename = craftablename.replace("axe", "battleaxe");
            }

            // get the craftable
            Craftable craftable = Craftables.instance().craftable(craftablename);
            if(craftable == null) {
                // not a craftable, just add the item
                loot.add(genstack);
            } else {
                // make a craftable and add it
                loot.add(craftable.create());
            }
        }

        // generate a number of drops to add to the minecraft loot
        Location location = new Location(event.getLootContext().getLocation());
        int luck = locationluck.getOrDefault(location, 0);
        int extra = itemcount(false, luck);
        loot.addAll(LootDrops.instance().drops(location.world(), luck, extra));

        // set the loot of the event
        event.setLoot(loot.stream().map(ItemStack::bukkitstack).toList());
    }
}
