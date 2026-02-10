package net.mctitan.rpg.util.stats;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.enums.ScoreboardStat;

import java.util.*;

public class DefenseStats implements Stats {
    private final double health;
    private final double maxhealth;
    private final int mana;
    private final int maxmana;
    private final double manaregen;
    private final Map<DamageType, Integer> defenses = new HashMap<>();

    public DefenseStats(Player player) {
        this.health = player.health();
        this.maxhealth = player.maxhealth();
        this.mana = (int)player.mana().value();
        this.maxmana = player.mana().maximum();
        this.manaregen = player.mana().regen() * 20; // regen is per tick, convert to per second

        List<DamageType> damagetypes = List.of(DamageType.PHYSICAL, DamageType.BURN, DamageType.FROST, DamageType.SHOCK, DamageType.HAVOC);
        for(DamageType damagetype : damagetypes) {
            this.defenses.put(damagetype, player.defense().reductions().reduction(damagetype).defense());
        }
    }

    public Map<ScoreboardStat, Component> lines() {
        Map<ScoreboardStat, Component> lines = new LinkedHashMap<>();

        // defense line
        lines.put(ScoreboardStat.DEFENSE, MiniMessage.miniMessage().deserialize(String.format(
                "<gold>Defense: <gray>%d<gold>/<red>%d<gold>/<aqua>%d<gold>/<yellow>%d<gold>/<light_purple>%d",
                defenses.get(DamageType.PHYSICAL),
                defenses.get(DamageType.BURN),
                defenses.get(DamageType.FROST),
                defenses.get(DamageType.SHOCK),
                defenses.get(DamageType.HAVOC)
        )));

        // health line
        lines.put(ScoreboardStat.HEALTH, MiniMessage.miniMessage().deserialize(String.format(
                "<gold>Health: <green>%.01f<gold>/<green>%.01f",
                health,
                maxhealth
        )));

        // mana line
        lines.put(ScoreboardStat.MANA, MiniMessage.miniMessage().deserialize(String.format(
                "<gold>Mana: <green>%d<gold>/<green>%d <aqua>%.02f/s",
                mana,
                maxmana,
                manaregen
        )));

        return lines;
    }
}
