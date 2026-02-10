package net.mctitan.rpg.commands.pack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mctitan.commands.arguments.Argument;
import net.mctitan.commands.arguments.OptionalArgument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.commands.completers.ModifierNameCompleter;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.pack.PackModifiers;

public class AurivalePackModifierAddCommand extends AurivaleBaseCommand {
    private final Argument<ModifierSlot> slot = new Argument<>("slot", ModifierSlot.class);
    private final Argument<String> modifier = new Argument<>("modifier", String.class);
    private final OptionalArgument<Integer> rank = new OptionalArgument<>("rank", Integer.class, 1);

    public AurivalePackModifierAddCommand() {
        super("add");

        modifier.completer(new ModifierNameCompleter(PackModifiers.instance().modifiers()));

        this.add(slot);
        this.add(modifier);
        this.add(rank);
    }

    public boolean runCommand(Player player) {
        ModifierSlot slot = this.slot.value();
        if(slot == null) {
            slot = ModifierSlot.valueOf(this.slot.str().toUpperCase());
        }

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
