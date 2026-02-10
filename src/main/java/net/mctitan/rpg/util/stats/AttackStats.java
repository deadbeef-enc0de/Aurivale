package net.mctitan.rpg.util.stats;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.action.attack.AttackAction;
import net.mctitan.rpg.data.damage.DamagePart;
import net.mctitan.rpg.data.projectile.ProjectileSpeed;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.enums.ScoreboardStat;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AttackStats implements Stats {
    private static final List<DamageType> TYPES = List.of(DamageType.PHYSICAL, DamageType.BURN, DamageType.FROST, DamageType.SHOCK, DamageType.HAVOC);
    private final Map<DamageType, DamagePart> damages;
    private final double mindamage;
    private final double maxdamage;
    private final double attackspeed;
    private final double projectilespeed;
    private final double critchance;
    private final double critdamage;

    public AttackStats(Player player) {
        AttackAction attack = player.actions().attack();
        ProjectileSpeed projectilespeed = player.projectiles().speed();

        // setup individual damages
        Map<DamageType, DamagePart> rawdamages = attack.damage().damages();
        this.damages = new HashMap<>();
        for(DamageType damageType : TYPES) {
            if(!rawdamages.containsKey(damageType)) { continue; }
            DamagePart part = rawdamages.get(damageType);
            if(part.maximum() <= 0) { continue; }
            this.damages.put(damageType, part);
        }
        double min = 0;
        double max = 0;
        for(DamageType damagetype : damages.keySet()) {
            min += damages.get(damagetype).minimum();
            max += damages.get(damagetype).maximum();
        }
        this.mindamage = min;
        this.maxdamage = max;

        if(attack.attacktime().time() > 0) {
            this.attackspeed = 20d / attack.attacktime().time();
        } else {
            this.attackspeed = -1;
        }

        if(projectilespeed.value() > 0) {
            this.projectilespeed = projectilespeed.value();
        } else {
            this.projectilespeed = -1;
        }

        this.critchance = 100d * attack.critical().chance();
        this.critdamage = 100d * attack.critical().damage();
    }

    public Map<ScoreboardStat, Component> lines() {
        if(attackspeed < 0 && projectilespeed < 0) {
            return Map.of();
        }
        Map<ScoreboardStat, Component> lines = new LinkedHashMap<>();

        // add damage lines
        lines.put(ScoreboardStat.TOTAL_DAMAGE, MiniMessage.miniMessage().deserialize(String.format(
                "<gold>Damage: <green>%.01f - %.01f",
                mindamage,
                maxdamage
        )));

        // add individual damages if there are more than 1
        if(damages.size() > 1) {
            Component damageline = Component.text("");
            for (DamageType damagetype : TYPES) {
                if(!damages.containsKey(damagetype)) { continue; }
                DamagePart damage = damages.get(damagetype);
                damageline = damageline.append(MiniMessage.miniMessage().deserialize(String.format(
                        "<%s>%.01f-%.01f ",
                        damagetype.color() == ChatColor.BLUE ? "aqua" : damagetype.color().name().toLowerCase(),
                        damage.minimum(),
                        damage.maximum()
                )));
            }
            lines.put(ScoreboardStat.ALL_DAMAGES, damageline);
        } else {
            lines.put(ScoreboardStat.ALL_DAMAGES, null);
        }

        // add attack speed line
        if(attackspeed > 0) {
            lines.put(ScoreboardStat.ATTACK_SPEED, MiniMessage.miniMessage().deserialize(String.format(
                    "<gold>Attack Speed: <green>%.02f",
                    attackspeed
            )));
        } else {
            lines.put(ScoreboardStat.ATTACK_SPEED, null);
        }

        // add projectile speed line
        if(projectilespeed > 0) {
            lines.put(ScoreboardStat.PROJECTILE_SPEED, MiniMessage.miniMessage().deserialize(String.format(
                    "<gold>Proj. Speed: <green>%.02f",
                    projectilespeed
            )));
        } else {
            lines.put(ScoreboardStat.PROJECTILE_SPEED, null);
        }

        // add critical line
        lines.put(ScoreboardStat.CRITICAL, MiniMessage.miniMessage().deserialize(String.format(
                "<gold>Critical: <green>%.02f%%<gold>/<green>+%.02f%%",
                critchance,
                critdamage
        )));

        return lines;
    }
}
