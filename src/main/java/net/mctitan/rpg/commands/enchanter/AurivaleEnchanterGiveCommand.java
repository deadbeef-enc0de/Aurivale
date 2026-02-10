package net.mctitan.rpg.commands.enchanter;

import net.mctitan.commands.arguments.Argument;
import net.mctitan.commands.arguments.OptionalArgument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.commands.completers.ModifierNameCompleter;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.modifier.ModifierConfig;

public class AurivaleEnchanterGiveCommand extends AurivaleBaseCommand {
    private final Argument<EnchanterType> enchanter = new Argument<>("enchanter", EnchanterType.class);
    private final OptionalArgument<Integer> amount = new OptionalArgument<>("amount", Integer.class, 1);
    private final OptionalArgument<ModifierConfig> modifier = new OptionalArgument<>("modifier", ModifierConfig.class, null);

    public AurivaleEnchanterGiveCommand() {
        super("give");

        modifier.completer(ModifierNameCompleter.instance());

        this.add(enchanter);
        this.add(amount);
        this.add(modifier);
    }

    public boolean runCommand(Player player) {
        EnchanterType enchantertype = this.enchanter.value();
        if(enchantertype == null) {
            return true;
        }

        ItemStack stack = enchantertype.enchanter().stack();
        stack.bukkitstack().setAmount(this.amount.value());
        if(this.modifier.value() != null) {
            stack.enchantermod(this.modifier.value());
        }
        player.bukkitplayer().getInventory().addItem(stack.bukkitstack());

        return true;
    }
}
