package net.mctitan.rpg.data.pack;

import net.mctitan.data.BaseData;
import net.mctitan.data.UUID;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityPack extends BaseData {
    private UUID leader;
    private HashSet<UUID> followers = new HashSet<UUID>();

    public EntityPack() {}

    public EntityPack(Entity leader) {
        this.leader = leader.uuid;
        leader.pack(this);
    }

    public Entity leader() {
        if(Bukkit.getEntity(leader.uuid()) == null) { return null; }
        return DataManager.instance().entity(leader);
    }

    public Set<Entity> followers() {
        return followers.stream()
                .filter(uuid -> Bukkit.getEntity(uuid.uuid()) != null)
                .map(uuid -> DataManager.instance().entity(uuid))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public boolean contains(Entity entity) { return leader.equals(entity.uuid) || followers.contains(entity.uuid); }
    public void add(Entity entity) { followers.add(entity.uuid); entity.pack(this); }
    public void remove(Entity entity) { followers.remove(entity.uuid); entity.pack(null); }
}
