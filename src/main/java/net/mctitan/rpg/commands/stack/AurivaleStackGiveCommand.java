package net.mctitan.rpg.commands.stack;

import net.mctitan.commands.arguments.Argument;
import net.mctitan.commands.arguments.OptionalArgument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.util.Item;
import net.mctitan.rpg.util.Logger;
import org.bukkit.Material;

import java.util.logging.Level;

public class AurivaleStackGiveCommand extends AurivaleBaseCommand implements Logger {
    private final Argument<Material> material = new Argument<>("material", Material.class);
    private final OptionalArgument<Integer> amount = new OptionalArgument<>("amount", Integer.class, 1);

    public AurivaleStackGiveCommand() {
        super("give");

        this.add(material);
        this.add(amount);
    }

    public boolean runCommand(Player player) {
        ItemStack stack = Item.cleanstack(this.material.value(), this.amount.value());
        log(Level.INFO, "Giving "+player.bukkitplayer().getName()+" "+material.value()+"x"+amount.value());
        player.bukkitplayer().getInventory().addItem(stack.bukkitstack());
        return true;
    }
}
