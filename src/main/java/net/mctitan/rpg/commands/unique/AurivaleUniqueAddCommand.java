package net.mctitan.rpg.commands.unique;

import net.mctitan.commands.arguments.Argument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.commands.completers.UniqueCompleter;
import net.mctitan.rpg.commands.completers.UniqueModifierCompleter;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.unique.Unique;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class AurivaleUniqueAddCommand extends AurivaleBaseCommand {
    private final Argument<Unique> unique = new Argument<>("unique", Unique.class);
    private final Argument<String> modifier = new Argument<>("modifier", String.class);

    private final UniqueModifierCompleter uniquemodcompleter = new UniqueModifierCompleter();

    public AurivaleUniqueAddCommand() {
        super("add");

        this.unique.completer(new UniqueCompleter());
        this.modifier.completer(uniquemodcompleter);

        this.add(this.unique);
        this.add(this.modifier);
    }

    public boolean runCommand(Player player) {
        Unique unique = this.unique.value();
        if(unique == null) {
            return true;
        }

        ModifierTemplate template = unique.modifier(this.modifier.value());
        if(template == null) {
            return true;
        }

        ItemStack inhand = new ItemStack(player.bukkitplayer().getEquipment().getItemInMainHand());
        template.modifier().apply(ModifierSlot.UNIQUE, inhand);

        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length == 2) {
            uniquemodcompleter.set(args[0]);
        }

        return super.onTabComplete(sender, command, alias, args);
    }
}
