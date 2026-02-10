package net.mctitan.rpg.util.stats;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.action.spell.SpellActionData;
import net.mctitan.rpg.enums.ScoreboardStat;
import net.mctitan.rpg.enums.SpellActivation;
import net.mctitan.rpg.spell.Spells;
import net.mctitan.rpg.util.Text;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SpellStats implements Stats {
    private final List<SpellActionData> spelldata;
    private final int mana;

    public SpellStats(Player player) {
        List<SpellActionData> spelldata = player.actions().spell().spells(SpellActivation.CAST);
        this.spelldata = new LinkedList<>();
        for(SpellActionData data : spelldata) {
            int level = player.actions().spell().level().value(data.level);
            this.spelldata.add(new SpellActionData(data.spelltype, level));
        }
        this.mana = Spells.instance().mana(player, spelldata);
    }

    public Map<ScoreboardStat, Component> lines() {
        if(spelldata.isEmpty()) {
            return Map.of();
        }
        Map<ScoreboardStat, Component> lines = new LinkedHashMap<>();

        int spellnum = 0;
        for(SpellActionData data : spelldata) {
            // add spell
            lines.put(ScoreboardStat.spell(spellnum), MiniMessage.miniMessage().deserialize(String.format(
                    "<gold>%s <green>%s",
                    data.spelltype.spell().name(),
                    Text.romanint(data.level)
            )));

            // make sure we only add 3 spells
            ++spellnum;
            if(spellnum >= 3) { break; }
        }

        lines.put(ScoreboardStat.MANA_COST, MiniMessage.miniMessage().deserialize(String.format(
                "<gold>Cost: <green>%d Mana",
                mana
        )));

        return lines;
    }
}
