package net.mctitan.rpg.commands.pack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mctitan.commands.arguments.Argument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.pack.PackModifiers;

import java.util.List;

public class AurivalePackGenmodsCommand extends AurivaleBaseCommand {
    private final Argument<Integer> groups = new Argument<>("group", Integer.class);
    private final Argument<Integer> modifiers = new Argument<>("modifiers", Integer.class);

    public AurivalePackGenmodsCommand() {
        super("genmods");

        this.add(groups);
        this.add(modifiers);
    }

    public boolean runCommand(Player player) {
        if(groups.value() == null || groups.value() < 1) {
            return true;
        }

        if(modifiers.value() == null || groups.value() < 1) {
            return true;
        }

        player.bukkitplayer().sendMessage(
                Component.text(String.format("Getting %d modifier(s) from %d group(s)", modifiers.value(), groups.value()))
                        .color(NamedTextColor.GOLD)
        );
        List<Modifier> mods = PackModifiers.instance().modifiers(groups.value(), modifiers.value());
        for(Modifier mod : mods) {
            player.bukkitplayer().sendMessage(Component.text("  ")
                    .append(Component.text(String.format("%d) ", mod.rank())).color(NamedTextColor.GOLD))
                    .append(Component.text(String.format("%s: ", mod.id())).color(NamedTextColor.DARK_AQUA))
                    .append(mod.text().color(NamedTextColor.GREEN))
            );
        }

        return true;
    }
}
