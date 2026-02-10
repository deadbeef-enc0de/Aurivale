package net.mctitan.rpg.commands;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.commands.craftable.AurivaleCraftableCommand;
import net.mctitan.rpg.commands.enchanter.AurivaleEnchanterCommand;
import net.mctitan.rpg.commands.loot.AurivaleLootCommand;
import net.mctitan.rpg.commands.modifier.AurivaleModifierCommand;
import net.mctitan.rpg.commands.pack.AurivalePackCommand;
import net.mctitan.rpg.commands.spell.AurivaleSpellCommand;
import net.mctitan.rpg.commands.stack.AurivaleStackCommand;
import net.mctitan.rpg.commands.trade.AurivaleTradeCommand;
import net.mctitan.rpg.commands.unique.AurivaleUniqueCommand;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.ScoreboardStat;
import net.mctitan.rpg.util.stats.PlayerStats;

import java.util.Map;

public class AurivaleCommand extends AurivaleBaseCommand {
    public AurivaleCommand() {
        super("rpg");
        this.server(true);

        if(Aurivale.instance().getConfig("debug").getBoolean("command")) {
            this.add(new AurivaleCraftableCommand());
            this.add(new AurivaleEnchanterCommand());
            this.add(new AurivaleGiveCommand());
            this.add(new AurivaleLootCommand());
            this.add(new AurivaleModifierCommand());
            this.add(new AurivalePackCommand());
            this.add(new AurivaleSpellCommand());
            this.add(new AurivaleStackCommand());
            this.add(new AurivaleTeleportCommand());
            this.add(new AurivaleTradeCommand());
            this.add(new AurivaleUniqueCommand());
        }
        this.add(new AurivaleHandbookCommand());
    }

    public boolean runCommand(Player player) {
        // create player stats
        PlayerStats playerstats = new PlayerStats(player);

        // send player each line in order
        player.bukkitplayer().sendMessage(Component.text(" "));
        Map<ScoreboardStat, Component> lines = playerstats.lines();
        for(ScoreboardStat scoreboardstat : ScoreboardStat.values()) {
            Component component = lines.get(scoreboardstat);
            if(component != null) {
                player.bukkitplayer().sendMessage(component);
            }
        }

        return true;
    }
}
