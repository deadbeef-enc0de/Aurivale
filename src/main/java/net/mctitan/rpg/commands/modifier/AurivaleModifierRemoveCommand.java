package net.mctitan.rpg.commands.modifier;

import net.mctitan.commands.arguments.Argument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.commands.completers.ModifierNameCompleter;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.enums.ModifierSlot;

public class AurivaleModifierRemoveCommand extends AurivaleBaseCommand {
    private final Argument<ModifierSlot> slot = new Argument<>("slot", ModifierSlot.class);
    private final Argument<ModifierConfig> modifier = new Argument<>("modifier", ModifierConfig.class);

    public AurivaleModifierRemoveCommand() {
        super("remove");

        modifier.completer(ModifierNameCompleter.instance());

        this.add(slot);
        this.add(modifier);
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

        Modifier modifier = null;
        ItemStack inhand = new ItemStack(player.bukkitplayer().getInventory(), player.bukkitplayer().getInventory().getHeldItemSlot());
        for(Modifier m : inhand.modifiers(slot)) {
            if(m.id().equals(config.id())) {
                modifier = m;
                break;
            }
        }

        if(modifier == null) {
            return true;
        }

        modifier.unapply(slot, inhand);

        return true;
    }
}
