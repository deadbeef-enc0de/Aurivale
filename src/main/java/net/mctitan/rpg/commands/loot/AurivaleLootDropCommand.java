package net.mctitan.rpg.commands.loot;

import net.mctitan.commands.arguments.OptionalArgument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.loot.LootDrops;
import org.bukkit.World;

import java.util.List;

public class AurivaleLootDropCommand extends AurivaleBaseCommand {
    private OptionalArgument<Integer> amount = new OptionalArgument<>("amount", Integer.class, 1);
    private OptionalArgument<Integer> luck = new OptionalArgument<>("luck", Integer.class, 0);

    public AurivaleLootDropCommand() {
        super("drop");

        this.add(amount);
        this.add(luck);
    }

    public boolean runCommand(Player player) {
        if(amount.value() == null || luck.value() == null) {
            return true;
        }

        World world = player.bukkitplayer().getWorld();
        List<ItemStack> items = LootDrops.instance().drops(player, luck.value(), amount.value());
        for(ItemStack item : items) {
            world.dropItemNaturally(player.bukkitplayer().getLocation(), item.bukkitstack());
        }

        return true;
    }
}
