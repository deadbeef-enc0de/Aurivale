package net.mctitan.rpg.commands.pack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mctitan.commands.arguments.Argument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.commands.completers.ModifierNameCompleter;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.pack.PackModifiers;

public class AurivalePackModifierInfoCommand extends AurivaleBaseCommand {
    private final Argument<String> modifier = new Argument<>("modifier", String.class);

    public AurivalePackModifierInfoCommand() {
        super("info");

        modifier.completer(new ModifierNameCompleter(PackModifiers.instance().modifiers()));

        this.add(modifier);
    }

    public boolean runCommand(Player player) {
        if(modifier.value() == null || modifier.value().isEmpty()) {
            return true;
        }

        ModifierConfig config = PackModifiers.instance().modifier(modifier.value());
        if(config == null) {
            Component message = Component.text(this.modifier.str()).color(NamedTextColor.GOLD)
                    .append(Component.text(" doest not exist").color(NamedTextColor.RED));
            player.bukkitplayer().sendMessage(message);
            return true;
        }

        int index = 0;
        Component[] messages = new Component[config.ranks().size() + 1];
        messages[index] = Component.text(config.id()).color(NamedTextColor.GOLD)
                .append(Component.text(": ").color(NamedTextColor.GOLD))
                .append(Component.text(config.ranks().size()).color(NamedTextColor.GREEN))
                .append(Component.text(" rank(s): ").color(NamedTextColor.GOLD));
        for(int rank : config.ranks()) {
            messages[++index] = Component.text("  ")
                    .append(Component.text(rank).color(NamedTextColor.GOLD))
                    .append(Component.text(": ").color(NamedTextColor.GOLD))
                    .append(Component.text(config.template(rank).string()).color(NamedTextColor.GREEN));
        }

        for(Component message : messages) {
            player.bukkitplayer().sendMessage(message);
        }

        return true;
    }
}
