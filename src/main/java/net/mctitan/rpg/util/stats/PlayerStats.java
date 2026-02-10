package net.mctitan.rpg.util.stats;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.ScoreboardStat;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerStats implements Stats {
    private final AttackStats attack;
    private final SpellStats spell;
    private final DefenseStats defense;
    private final MiscStats misc;

    public PlayerStats(Player player) {
        attack = new AttackStats(player);
        spell = new SpellStats(player);
        defense = new DefenseStats(player);
        misc = new MiscStats(player);
    }

    public Map<ScoreboardStat, Component> lines() {
        Map<ScoreboardStat, Component> lines = new LinkedHashMap<>();
        lines.putAll(attack.lines());
        lines.putAll(spell.lines());
        lines.putAll(defense.lines());
        lines.putAll(misc.lines());
        return lines;
    }
}
