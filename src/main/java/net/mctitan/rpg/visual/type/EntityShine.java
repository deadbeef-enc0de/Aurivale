package net.mctitan.rpg.visual.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.VisualType;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.visual.EntityVisual;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

public class EntityShine extends EntityVisual {
    private int red;
    private int green;
    private int blue;
    private transient Color color;

    public EntityShine() {}

    public EntityShine(Entity entity, Color color) {
        super(entity);

        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
        this.color = color;
    }

    public VisualType type() { return VisualType.ENTITY_SHINE; }

    public void initialize() {
        this.color = Color.fromRGB(red, green, blue);
    }

    public void tick() {
        if(entity() == null || entity().bukkitentity() == null) {
            return;
        }

        // get entity bounding box
        Entity entity = entity();
        BoundingBox boundingbox =  entity.bukkitentity().getBoundingBox();

        // make 5 particles
        for(int i = 0; i < 5; ++i) {
            // get random location
            World world = entity.bukkitentity().getWorld();
            double x = Math.nextDouble(boundingbox.getMinX(), boundingbox.getMaxX());
            double y = Math.nextDouble(boundingbox.getMinY(), boundingbox.getMaxY());
            double z = Math.nextDouble(boundingbox.getMinZ(), boundingbox.getMaxZ());
            Location location = new Location(world, x, y, z);

            // spawn particles
            world.spawnParticle(Particle.DUST, location.bukkit(), 1, 0.1, 0.1, 0.1, 0, new Particle.DustOptions(color, 1));
        }
    }
}
