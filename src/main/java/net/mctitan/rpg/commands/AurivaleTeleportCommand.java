package net.mctitan.rpg.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mctitan.commands.arguments.Argument;
import net.mctitan.rpg.commands.completers.WorldCompleter;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.teleporter.Teleporter;
import net.mctitan.rpg.util.teleporter.sequence.AdjacentLocationSequence;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class AurivaleTeleportCommand extends AurivaleBaseCommand {
    private final Argument<String> world = new Argument<>("world", String.class);

    public AurivaleTeleportCommand() {
        super("teleport");

        this.world.completer(new WorldCompleter());

        this.add(world);
    }

    public boolean runCommand(Player player) {
        // get world to teleport to
        World world = Bukkit.getWorld(this.world.value());

        // check for no world
        if(world == null) {
            player.bukkitplayer().sendMessage(
                    Component.text(String.format("Invalid world=%s", this.world.str()))
                            .color(NamedTextColor.RED)
            );
            return true;
        }

        // check for same world
        if(world == player.bukkitplayer().getWorld()) {
            player.bukkitplayer().sendMessage(
                    Component.text(String.format("Already in world=%s cannot teleport", this.world.str()))
                            .color(NamedTextColor.RED)
            );
            return true;
        }

        // get location to teleport player to
        Location location = new Location(player.bukkitplayer().getLocation());
        location.world(world);

        // teleport player
        AdjacentLocationSequence sequence = new AdjacentLocationSequence(location);
        Teleporter teleporter = Teleporter.teleport(player, sequence);
        teleporter.water(false);
        teleporter.lava(false);

        return true;
    }
}
