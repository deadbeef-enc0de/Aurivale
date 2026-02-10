package net.mctitan.rpg.dimension;

import net.mctitan.rpg.data.dimension.Dimension;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public class Handler implements Listener {
    private Dimension dimension;
    private Set<EntityType> entities = new HashSet<>();
    private Set<Object> actions = new HashSet<>();

    public World world() { return dimension.world(); }
    public Dimension dimension() { return dimension; }
    public void dimension(Dimension dimension) { this.dimension = dimension; }

    public void permissions(ConfigurationSection section) {
    }
}
