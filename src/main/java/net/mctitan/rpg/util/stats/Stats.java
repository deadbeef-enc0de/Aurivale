package net.mctitan.rpg.util.stats;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.enums.ScoreboardStat;

import java.util.Map;

public interface Stats {
    Map<ScoreboardStat, Component> lines();
}
