package net.mctitan.rpg.commands.enchanter;

import net.mctitan.commands.arguments.Argument;
import net.mctitan.commands.arguments.OptionalArgument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.commands.completers.ModifierNameCompleter;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.modifier.ModifierConfig;

public class AurivaleEnchanterApplyCommand extends AurivaleBaseCommand {
    private final Argument<EnchanterType> enchanter = new Argument<>("enchanter", EnchanterType.class);
    private final OptionalArgument<Integer> skill = new OptionalArgument<>("skill", Integer.class, 0);
    private final OptionalArgument<ModifierConfig> modifier = new OptionalArgument<>("modifier", ModifierConfig.class, null);

    public AurivaleEnchanterApplyCommand() {
        super("apply");

        modifier.completer(ModifierNameCompleter.instance());

        this.add(enchanter);
        this.add(skill);
        this.add(modifier);
    }

    public boolean runCommand(Player player) {
        EnchanterType enchantertype = this.enchanter.value();
        if (enchantertype == null) {
            return true;
        }

        ItemStack enchanter = enchantertype.enchanter().stack();
        if(this.modifier.value() != null) {
            ModifierConfig modifierconfig = this.modifier.value();
            enchanter.enchantermod(modifierconfig);
        }

        ItemStack inhand = new ItemStack(player.bukkitplayer().getInventory(), player.bukkitplayer().getInventory().getHeldItemSlot());
        if(inhand.craftable() == null) {
            return true;
        }

        enchantertype.enchanter().enchant(player, enchanter, inhand, this.skill.value());
        return true;
    }
}
