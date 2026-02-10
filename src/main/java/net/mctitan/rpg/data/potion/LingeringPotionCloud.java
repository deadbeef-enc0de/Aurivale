package net.mctitan.rpg.data.potion;

import net.mctitan.data.BaseData;
import net.mctitan.data.UUID;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.util.directional.ChunkLocation;
import org.bukkit.entity.AreaEffectCloud;

import java.util.HashSet;

public class LingeringPotionCloud extends BaseData {
    private HashSet<UUID> applications = new HashSet<>();
    private PotionInfo potioninfo;
    private ChunkLocation chunklocation;
    private int lived = 0;
    private transient int sessionlived = 0;

    public LingeringPotionCloud() {}

    public LingeringPotionCloud(AreaEffectCloud cloud, ItemStack potion) {
        super(cloud.getUniqueId());
        potioninfo = new PotionInfo(potion.modifiers());
        chunklocation = new ChunkLocation(cloud.getLocation());
    }

    public LingeringPotionCloud(AreaEffectCloud cloud, Entity entity) {
        super(cloud.getUniqueId());
        potioninfo = new PotionInfo(entity);
        chunklocation = new ChunkLocation(cloud.getLocation());
    }

    public void disable() { lived = sessionlived; }

    public PotionInfo potioninfo() { return potioninfo; }
    public float radius() { return (float)potioninfo.area(); }
    public int duration() { return potioninfo.duration(); }
    public ChunkLocation chunklocation() { return chunklocation; }
    public int lived() { return lived; }

    public void sessionlived(int sessionlived) { this.sessionlived = sessionlived; }

    public boolean canapply(Entity entity) {
        return !applications.contains(entity.uuid);
    }

    public void apply(Entity entity) {
        // make sure we can apply the cloud
        if(!canapply(entity)) {
            return;
        }

        // put the entity time into mapping of when the next time the entity
        applications.add(entity.uuid);

        // apply the potion cloud to the entity
        potioninfo.apply(entity);
    }
}
