package net.mctitan.rpg.trading.groups;

import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.crafting.Craftables;
import net.mctitan.rpg.trading.Profession;
import net.mctitan.rpg.trading.TradeGroup;
import net.mctitan.rpg.trading.trades.CraftableTrade;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public class CraftableTradeGroup extends TradeGroup implements Logger {
    public CraftableTradeGroup(Profession profession, String name, int weight, ConfigurationSection section) {
        super(profession, name, weight);

        // get craftable trades
        for(String craftablename : section.getStringList("craftables")) {
            Craftable craftable = Craftables.instance().craftable(craftablename);
            if(craftable == null) {
                log(Level.WARNING, String.format("Profession=%s tradegroup=%s craftable=%s not found",
                        profession().name(),
                        name(),
                        craftablename
                ));
                continue;
            }

            log(Level.INFO, String.format("Profession=%s tradegroup=%s added craftable trade for %s",
                    profession().name(),
                    name(),
                    craftablename
            ));
            add(new CraftableTrade(craftable));
        }
    }
}
