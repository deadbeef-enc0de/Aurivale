package net.mctitan.rpg.trading.groups;

import net.mctitan.rpg.trading.Profession;
import net.mctitan.rpg.trading.TradeGroup;
import net.mctitan.rpg.trading.trades.ItemTrade;
import net.mctitan.rpg.util.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public class ItemTradeGroup extends TradeGroup implements Logger {
    public ItemTradeGroup(Profession profession, String name, int weight, ConfigurationSection section) {
        super(profession, name, weight);

        // get item trades
        for(String materialname : section.getStringList("items")) {
            Material material = Material.valueOf(materialname);
            if(material == null) {
                log(Level.WARNING, String.format("Profession=%s tradegroup=%s material=%s not found",
                        profession().name(),
                        name(),
                        materialname
                ));
                continue;
            }

            log(Level.INFO, String.format("Profession=%s tradegroup=%s added item trade for %s",
                    profession().name(),
                    name(),
                    materialname
            ));
            add(new ItemTrade(material));
        }
    }
}
