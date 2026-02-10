package net.mctitan.rpg.potion;

import net.mctitan.data.UUID;
import net.mctitan.data.saver.WorldSaver;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.potion.LingeringPotionCloud;
import net.mctitan.rpg.util.directional.ChunkLocation;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LingeringPotionClouds {
    private static final LingeringPotionClouds instance = new LingeringPotionClouds();

    private Map<World, WorldSaver> worlds = new HashMap<>();
    private Map<UUID, LingeringPotionCloud> clouds = new HashMap<>();

    public LingeringPotionClouds() {}

    public static LingeringPotionClouds instance() { return instance; }

    public void disable() {
        synchronized (clouds) {
            for(LingeringPotionCloud cloud : clouds.values()) {
                // try to get the AreaEffectCloud to update ticks lived
                AreaEffectCloud areacloud = (AreaEffectCloud)Bukkit.getEntity(cloud.uuid.uuid());
                if(areacloud != null) {
                    int lived = cloud.lived() + areacloud.getTicksLived();
                    cloud.sessionlived(lived);
                }

                // diable cloud and save
                cloud.disable();
                save(cloud);
            }
        }
    }

    public LingeringPotionCloud cloud(java.util.UUID uuid) { return cloud(new UUID(uuid)); }
    public LingeringPotionCloud cloud(UUID uuid) { synchronized (clouds) { return clouds.get(uuid); } }

    public LingeringPotionCloud create(AreaEffectCloud areacloud, ItemStack potion) {
        // create lingering cloud, add it, and save it
        LingeringPotionCloud cloud = new LingeringPotionCloud(areacloud, potion);
        add(cloud);
        save(cloud);

        // set base potion type to something meaningless so it works
        areacloud.setBasePotionType(PotionType.LUCK);

        // set the color of the potion cloud
        PotionMeta potionmeta = (PotionMeta)potion.bukkitstack().getItemMeta();
        if(potionmeta.getColor() != null) {
            areacloud.setColor(potionmeta.getColor());
        } else {
            areacloud.setColor(Color.BLACK);
        }

        // set duration info on cloud
        areacloud.setDuration(cloud.duration());
        areacloud.setDurationOnUse(0);

        // set radius info on cloud
        areacloud.setRadius(cloud.radius());
        areacloud.setRadiusOnUse(0);
        areacloud.setRadiusPerTick(0);

        return cloud;
    }

    public LingeringPotionCloud create(Entity entity) {
        // create the bukkit cloud
        Location location = new Location(entity.bukkitentity().getLocation());
        AreaEffectCloud areacloud = (AreaEffectCloud)location.world().spawnEntity(location.center().bukkit(), EntityType.AREA_EFFECT_CLOUD);
        areacloud.setColor(Color.GREEN);

        // create the lingering cloud
        LingeringPotionCloud cloud = new LingeringPotionCloud(areacloud, entity);
        add(cloud);
        save(cloud);

        // set base potion type to something meaningless so it works
        areacloud.setBasePotionType(PotionType.LUCK);

        // set duration info on cloud
        areacloud.setDuration(cloud.duration());
        areacloud.setDurationOnUse(0);

        // set radius info on cloud
        Creeper creeper = (Creeper) entity.bukkitentity();
        areacloud.setRadius(creeper.getExplosionRadius());
        areacloud.setRadiusOnUse(0);
        areacloud.setRadiusPerTick(0);

        return cloud;
    }

    private void add(LingeringPotionCloud cloud) {
        synchronized (clouds) { clouds.put(cloud.uuid, cloud); }
    }

    private WorldSaver saver(World world) {
        synchronized (worlds) {
            if (!worlds.containsKey(world)) {
                worlds.put(world, WorldSaver.saver(Aurivale.instance(), world));
            }
            return worlds.get(world);
        }
    }

    private void save(LingeringPotionCloud cloud) {
        ChunkLocation chunklocation = cloud.chunklocation();
        WorldSaver saver = saver(chunklocation.world());
        synchronized (saver) { saver.save(chunklocation.x(), chunklocation.z(), cloud); }
    }

    public void delete(LingeringPotionCloud cloud) {
        remove(cloud);

        // remove from disk
        ChunkLocation chunklocation = cloud.chunklocation();
        WorldSaver saver = saver(chunklocation.world());
        synchronized (saver) { saver.remove(chunklocation.x(), chunklocation.z(), cloud); }
    }

    private void remove(LingeringPotionCloud cloud) {
        // remove from the uuid mapping
        synchronized (clouds) { clouds.remove(cloud.uuid); }
    }

    public void chunkload(ChunkLocation chunklocation) {
        WorldSaver saver = saver(chunklocation.world());
        synchronized (saver) {
            Set<LingeringPotionCloud> loaded = saver.get(chunklocation.x(), chunklocation.z(), LingeringPotionCloud.class);
            for(LingeringPotionCloud cloud : loaded) {
                add(cloud);
            }
        }
    }

    public void chunkunload(ChunkLocation chunklocation) {
        WorldSaver saver = saver(chunklocation.world());
        synchronized (saver) {
            Set<LingeringPotionCloud> unloaded = saver.get(chunklocation.x(), chunklocation.z(), LingeringPotionCloud.class);
            for(LingeringPotionCloud cloud : unloaded) {
                remove(cloud);
            }
        }
    }
}
