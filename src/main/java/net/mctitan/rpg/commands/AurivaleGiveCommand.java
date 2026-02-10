package net.mctitan.rpg.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mctitan.commands.TabCompleter;
import net.mctitan.commands.arguments.Argument;
import net.mctitan.commands.arguments.OptionalArgument;
import net.mctitan.commands.data.Trie;
import net.mctitan.rpg.data.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

public class AurivaleGiveCommand extends AurivaleBaseCommand {
    private static final Set<Material> materials = Set.of(
            Material.REDSTONE, Material.GUNPOWDER, Material.SPIDER_EYE, Material.SUGAR, Material.BREWING_STAND, Material.BLAZE_POWDER
    );

    private final Argument<Material> material = new Argument<>("material", Material.class);
    private final OptionalArgument<Integer> amount = new OptionalArgument<>("amount", Integer.class, 1);

    public AurivaleGiveCommand() {
        super("give");

        this.material.completer(new GiveMaterialCompleter());

        this.add(material);
        this.add(amount);
    }

    public boolean runCommand(Player player) {
        if(material.value() == null || !materials.contains(material.value())) {
            player.bukkitplayer().sendMessage(Component.text(String.format("Invalid material %s", material.str())).color(NamedTextColor.RED));
            return true;
        }

        if(amount.value() == null || amount.value() < 1) {
            player.bukkitplayer().sendMessage(Component.text(String.format("Invalid amount %s", amount.str())).color(NamedTextColor.RED));
            return true;
        }

        player.bukkitplayer().give(new ItemStack(material.value(), amount.value()));
        player.bukkitplayer().sendMessage(Component.text(String.format("Given %d %s", amount.value(), material.str())).color(NamedTextColor.GREEN));

        return true;
    }

    private class GiveMaterialCompleter implements TabCompleter {
        private final Trie trie = new Trie();

        public GiveMaterialCompleter() {
            for(Material material : materials) {
                trie.add(material.name());
            }
        }

        public List<String> getPossibleEntries(String argument) { return trie.match(argument); }
    }
}
