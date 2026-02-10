package net.mctitan.rpg.commands.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mctitan.commands.arguments.Argument;
import net.mctitan.commands.arguments.OptionalArgument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.Spells;

public class AurivaleSpellCastCommand extends AurivaleBaseCommand {
    private final Argument<SpellType> spelltype = new Argument<>("spelltype", SpellType.class);
    private final Argument<Integer> level = new OptionalArgument<>("level", Integer.class, 1);

    public AurivaleSpellCastCommand() {
        super("cast");

        this.add(spelltype);
        this.add(level);
    }

    public boolean runCommand(Player player) {
        if(spelltype.value() == null) {
            player.bukkitplayer().sendMessage(Component
                    .text(String.format("%s is an invalid spell", spelltype.str()))
                    .color(NamedTextColor.RED));
            return true;
        }

        if(spelltype.value().spell() == null ||
           spelltype.value().instanceclass() == null) {
            player.bukkitplayer().sendMessage(Component
                    .text(String.format("%s is an incomplete spell", spelltype.str()))
                    .color(NamedTextColor.RED));
            return true;
        }

        if(level.value() == null || level.value() < 0) {
            player.bukkitplayer().sendMessage(Component
                    .text(String.format("%s is an invalid spell level", level.str()))
                    .color(NamedTextColor.RED));
            return true;
        }

        if(player.mana().value() < spelltype.value().spell().manacost(level.value())) {
            player.bukkitplayer().sendMessage(Component
                    .text(String.format("Player mana=%.01f is not enough for spell cost=%d",
                            player.mana().value(),
                            spelltype.value().spell().manacost(level.value())))
                    .color(NamedTextColor.RED));
            return true;
        }

        Spells.instance().cast(player, spelltype.value().spell(), level.value());
        return true;
    }
}
