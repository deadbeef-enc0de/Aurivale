package net.mctitan.rpg.commands.pack;

import net.mctitan.commands.arguments.Argument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.pack.PackManager;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class AurivalePackCreateCommand extends AurivaleBaseCommand {
    private final Argument<EntityType> entity = new Argument<>("entity", EntityType.class);

    public AurivalePackCreateCommand() {
        super("create");

        this.add(entity);
    }

    public boolean runCommand(Player player) {
        // make sure entity type is good
        if(entity.value() == null) { return true; }

        // get location player is looking at
        Block block = player.bukkitplayer().getTargetBlockExact(10);
        if(block == null) { return true; }
        Location location = new Location(block);
        location.y(location.y() + 1);

        // spawn entity
        LivingEntity bukkit = (LivingEntity)location.world().spawnEntity(location.bukkit(), entity.value(), SpawnReason.CUSTOM);
        Entity entity = DataManager.instance().entity(bukkit);

        // force create entity pack
        PackManager.instance().create(entity, true);

        return true;
    }
}
