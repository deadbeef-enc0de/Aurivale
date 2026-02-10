package net.mctitan.rpg.util.stats;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.ScoreboardStat;

import java.util.LinkedHashMap;
import java.util.Map;

public class MiscStats implements Stats {
    private final int crafting;
    private final int enchanting;
    private final int brewing;
    private final int dropluck;

    public MiscStats(Player player) {
        crafting = player.crafting().value();
        enchanting = player.enchanting().value();
        brewing = player.brewing().value();
        dropluck = player.dropluck().dropluck();
    }

    public Map<ScoreboardStat, Component> lines() {
        Map<ScoreboardStat, Component> lines = new LinkedHashMap<>();

        // add skills line
        if(crafting > 0 || enchanting > 0 || brewing > 0) {
            lines.put(ScoreboardStat.SKILLS, MiniMessage.miniMessage().deserialize(String.format(
                    "<gold>Skills: <green>%s<gold>/<green>%d<gold>/<green>%d",
                    crafting,
                    enchanting,
                    brewing
            )));
        }

        // add luck line
        if(dropluck > 0) {
            lines.put(ScoreboardStat.DROP_LUCK, MiniMessage.miniMessage().deserialize(String.format(
                    "<gold>Drop Luck: <green>%s",
                    dropluck
            )));
        }

        return lines;
    }
}
