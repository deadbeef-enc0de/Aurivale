package net.mctitan.rpg.visual;

import net.mctitan.data.UUID;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.util.directional.Location;

public abstract class EntityVisual extends Visual {
    private UUID entity;

    public EntityVisual() {}

    public EntityVisual(Entity entity) {
        this.entity = entity.uuid;
    }

    public UUID entityuuid() { return entity; }
    public Entity entity() { return DataManager.instance().entity(entity); }

    public Location location() {
        Entity entity = DataManager.instance().entity(this.entity);
        if(entity == null || entity.bukkitentity() == null) {
            return null;
        }

        return new Location(entity.bukkitentity().getLocation());
    }
}
