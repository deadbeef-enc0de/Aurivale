package net.mctitan.rpg.handler.general;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.potion.LingeringPotionCloud;
import net.mctitan.rpg.data.potion.PotionInfo;
import net.mctitan.rpg.potion.LingeringPotionClouds;
import net.mctitan.rpg.util.directional.ChunkLocation;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class Potions implements Listener {
    private static final Potions instance = new Potions();
    private static final double SPLASH_POTION_HEIGHT = 2d;
    private static final double SPLASH_POTION_CHECK_DISTANCE = 0.125d;

    public Potions() {}

    public static Potions instance() { return instance; }

    boolean infinitepotions = Aurivale.instance().getConfig("debug").getBoolean("infinite_potions");

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        // get the player
        Player player = DataManager.instance().player(event.getPlayer());
        if(player == null) {
            return;
        }

        // make sure the item consumed is a potion
        ItemStack stack = new ItemStack(event.getItem());
        if(stack.bukkitstack().getType() != Material.POTION &&
           stack.bukkitstack().getType() != Material.SPLASH_POTION &&
           stack.bukkitstack().getType() != Material.LINGERING_POTION) {
            return;
        }

        // get potion info and apply it to the player
        PotionInfo potioninfo = new PotionInfo(stack.modifiers());
        potioninfo.apply(player);

        // potions are infinite
        if(infinitepotions) {
            // set potion cooldown
            player.cooldown().set(stack.uuid(), potioninfo);
            ItemCooldown.instance().setcoolddown(player, stack);

            // cancel the event since we want to keep the potion
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPotionThrow(ProjectileLaunchEvent event) {
        // potions are not infinite, bail
        if(!infinitepotions) {
            return;
        }

        // make sure it's a thrown potion
        if(event.getEntity().getType() != EntityType.SPLASH_POTION &&
           event.getEntity().getType() != EntityType.LINGERING_POTION) {
            return;
        }

        // make sure shooter is a living entity
        if(!(event.getEntity().getShooter() instanceof LivingEntity bukkitentity)) {
            return;
        }
        Entity entity = DataManager.instance().entity(bukkitentity);

                // get original thrown potion
        ThrownPotion original = (ThrownPotion) event.getEntity();
        ItemStack stack = new ItemStack(original.getItem());

        // if the potion is in the thrown potion set, remove it and bail
        if(stack.uuid() == null) {
            return;
        }

        // set potion cooldown
        if(entity instanceof Player player) {
            PotionInfo potioninfo = new PotionInfo(stack.modifiers());
            player.cooldown().set(stack.uuid(), potioninfo);
            ItemCooldown.instance().setcoolddown(player, stack);
        }

        // get location and velocity of the thrown potion
        Location location = new Location(original.getLocation());
        Vector velocity = new Vector(original.getVelocity());

        // clone the item stack and remove the uuid
        stack = stack.clone();

        // make a new thrown potion entity and set details
        World world = location.world();
        boolean splash = event.getEntity().getType() == EntityType.SPLASH_POTION;
        Class<? extends ThrownPotion> clazz = splash ? SplashPotion.class : LingeringPotion.class;
        ThrownPotion thrown = world.spawn(location.bukkit(), clazz);
        thrown.setVelocity(velocity.bukkit());
        thrown.setItem(stack.bukkitstack());
        thrown.setShooter(original.getShooter());
        thrown.setPotionMeta(original.getPotionMeta());

        // cancel original event
        event.setCancelled(true);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        // get the potion info
        ItemStack stack = new ItemStack(event.getPotion().getItem());
        PotionInfo potioninfo = new PotionInfo(stack.modifiers());

        // get affected entities
        Location potionloc = new Location(event.getPotion().getLocation());
        double radius = potioninfo.area() + SPLASH_POTION_CHECK_DISTANCE;
        double height = SPLASH_POTION_HEIGHT + SPLASH_POTION_CHECK_DISTANCE;
        for(LivingEntity livingentity : potionloc.bukkit().getNearbyLivingEntities(radius, height)) {
            // make sure the entity is within the euclidean distance of the potion area
            if(livingentity.getLocation().distance(potionloc.bukkit()) > potioninfo.area()) {
                continue;
            }

            // if the entity does not have line of sight to potion, do nothing
            if(!livingentity.hasLineOfSight(event.getPotion())) {
                continue;
            }

            // get the entity and apply the potion to it
            Entity entity = DataManager.instance().entity(livingentity);
            if(entity != null) {
                potioninfo.apply(entity);
            }
        }
    }

    @EventHandler
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        // get potion and area cloud
        ItemStack potion = new ItemStack(event.getEntity().getItem());
        AreaEffectCloud areacloud = event.getAreaEffectCloud();

        // create the lingering potion cloud
        LingeringPotionClouds.instance().create(areacloud, potion);
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        // we only care about creepers
        if(event.getEntityType() != EntityType.CREEPER) {
            return;
        }

        // get the entity and see if it has any effects
        Creeper creeper = (Creeper) event.getEntity();
        Entity entity = DataManager.instance().entity(creeper);
        if(entity.statuseffects().numbereffects() == 0) {
            return;
        }

        // we have some effects so create the cloud from the entity
        LingeringPotionClouds.instance().create(entity);

        // remove all status effects to prevent vanilla cloud effect
        entity.statuseffects().removeall();
    }

    @EventHandler
    public void onAreaEffectCloudRemoved(EntityRemoveFromWorldEvent event) {
        // we only care about area effect clouds
        if(event.getEntityType() != EntityType.AREA_EFFECT_CLOUD) {
            return;
        }

        // get the potion cloud
        AreaEffectCloud areacloud = (AreaEffectCloud)event.getEntity();
        LingeringPotionCloud cloud = LingeringPotionClouds.instance().cloud(areacloud.getUniqueId());
        if(cloud == null) {
            return;
        }

        // make sure the cloud duration is over
        int lived = cloud.lived() + areacloud.getTicksLived();
        if(lived < cloud.duration()) {
            cloud.sessionlived(lived);
            return;
        }

        // remove the cloud
        LingeringPotionClouds.instance().delete(cloud);
    }

    @EventHandler
    public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
        // cancel the event, the applied effect is a dummy
        event.setCancelled(true);

        // get the cloud
        LingeringPotionCloud cloud = LingeringPotionClouds.instance().cloud(event.getEntity().getUniqueId());
        if(cloud == null) {
            event.getEntity().remove();
            return;
        }

        // apply the cloud to the affected entities
        for(LivingEntity livingentity : event.getAffectedEntities()) {
            Entity entity = DataManager.instance().entity(livingentity);
            if(entity == null) {
                continue;
            }

            if(cloud.canapply(entity)) {
                cloud.apply(entity);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        LingeringPotionClouds.instance().chunkload(new ChunkLocation(event.getChunk()));
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        LingeringPotionClouds.instance().chunkunload(new ChunkLocation(event.getChunk()));
    }
}
