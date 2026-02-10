package net.mctitan.rpg.commands.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mctitan.commands.arguments.Argument;
import net.mctitan.commands.arguments.OptionalArgument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.crafting.Craftables;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.enums.SpellActivation;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.SpellTemplate;

public class AurivaleSpellWandCommand extends AurivaleBaseCommand {
    private final Argument<SpellType> spelltype = new Argument<>("spelltype", SpellType.class);
    private final Argument<Integer> level = new OptionalArgument<>("level", Integer.class, 1);

    public AurivaleSpellWandCommand() {
        super("wand");

        this.add(spelltype);
        this.add(level);
    }

    public boolean runCommand(Player player) {
        if (spelltype.value() == null) {
            player.bukkitplayer().sendMessage(Component
                    .text(String.format("%s is an invalid spell", spelltype.str()))
                    .color(NamedTextColor.RED));
            return true;
        }

        if (spelltype.value().spell() == null ||
                spelltype.value().instanceclass() == null) {
            player.bukkitplayer().sendMessage(Component
                    .text(String.format("%s is an incomplete spell", spelltype.str()))
                    .color(NamedTextColor.RED));
            return true;
        }

        if (level.value() == null || level.value() < 0) {
            player.bukkitplayer().sendMessage(Component
                    .text(String.format("%s is an invalid spell level", level.str()))
                    .color(NamedTextColor.RED));
            return true;
        }

        // get the spell modifier
        ModifierTemplate template = new SpellTemplate(level.value(), true, false, "wand_spell_cmd",
                spelltype.value(), SpellActivation.CAST, 0, level.value(), 0);
        Modifier modifier = template.modifier();

        // get the wand and apply the modifier
        ItemStack stack = Craftables.instance().craftable("wand").create();
        modifier.apply(ModifierSlot.BASE, stack);

        // give stack to player
        player.bukkitplayer().getInventory().addItem(stack.bukkitstack());

        return true;
    }
}
