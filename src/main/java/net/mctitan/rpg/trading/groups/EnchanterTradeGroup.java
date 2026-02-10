package net.mctitan.rpg.trading.groups;

import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.trading.Profession;
import net.mctitan.rpg.trading.TradeGroup;
import net.mctitan.rpg.trading.trades.EnchanterTrade;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public class EnchanterTradeGroup extends TradeGroup implements Logger {
    public EnchanterTradeGroup(Profession profession, String name, int weight, ConfigurationSection section) {
        super(profession, name, weight);

        // get enchanter trades
        for(String enchantertypename : section.getStringList("enchanters")) {
            EnchanterType enchantertype = EnchanterType.valueOf(enchantertypename);
            if(enchantertype == null) {
                log(Level.WARNING, String.format("Profession=%s tradegroup=%s enchanter=%s not found",
                        profession().name(),
                        name(),
                        enchantertypename
                ));
                continue;
            }

            log(Level.INFO, String.format("Profession=%s tradegroup=%s added enchanter trade for %s",
                    profession().name(),
                    name(),
                    enchantertypename
            ));
            add(new EnchanterTrade(enchantertype));
        }
    }
}
