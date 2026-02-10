package net.mctitan.rpg.commands.modifier;

import net.mctitan.commands.arguments.Argument;
import net.mctitan.commands.arguments.OptionalArgument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.commands.completers.ModifierNameCompleter;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.enums.ModifierSlot;

public class AurivaleModifierAddCommand extends AurivaleBaseCommand {
    private final Argument<ModifierSlot> slot = new Argument<>("slot", ModifierSlot.class);
    private final Argument<ModifierConfig> modifier = new Argument<>("modifier", ModifierConfig.class);
    private final OptionalArgument<Integer> rank = new OptionalArgument<>("rank", Integer.class, 1);

    public AurivaleModifierAddCommand() {
        super("add");

        modifier.completer(ModifierNameCompleter.instance());

        this.add(slot);
        this.add(modifier);
        this.add(rank);
    }

    public boolean runCommand(Player player) {
        ModifierSlot slot = this.slot.value();
        if(slot == null) {
            slot = ModifierSlot.valueOf(this.slot.str().toUpperCase());
        }

        ModifierConfig config = this.modifier.value();
        if(config == null) {
            return true;
        }

        int rank = this.rank.value();
        if(!config.ranks().contains(rank)) {
            return true;
        }

        Modifier modifier = config.template(rank).modifier();
        if(slot == ModifierSlot.BASE) { modifier.base(true); }
        ItemStack inhand = new ItemStack(player.bukkitplayer().getInventory(), player.bukkitplayer().getInventory().getHeldItemSlot());
        inhand.addenchantment(Enchantment.mending);
        modifier.apply(slot, inhand);

        return true;
    }
}
