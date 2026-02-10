package net.mctitan.rpg.commands.craftable;

import net.mctitan.commands.arguments.Argument;
import net.mctitan.commands.arguments.OptionalArgument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.commands.completers.CraftableCompleter;
import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;

public class AurivaleCraftableGiveCommand extends AurivaleBaseCommand {
    private final Argument<Craftable> craftable = new Argument<>("craftable", Craftable.class);
    private final OptionalArgument<Integer> crafting = new OptionalArgument<>("crafting", Integer.class, 0);

    public AurivaleCraftableGiveCommand() {
        super("give");

        this.craftable.completer(new CraftableCompleter());

        this.add(craftable);
        this.add(crafting);
    }

    public boolean runCommand(Player player) {
        Craftable craftable = this.craftable.value();
        if(craftable == null || this.crafting.value() == null) {
            return true;
        }

        ItemStack stack = craftable.create(this.crafting.value());
        player.bukkitplayer().getInventory().addItem(stack.bukkitstack());

        return true;
    }
}
