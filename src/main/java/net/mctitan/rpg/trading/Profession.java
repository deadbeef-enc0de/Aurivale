package net.mctitan.rpg.trading;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.tables.weighted.WeightedTable;
import net.mctitan.rpg.enums.RandomType;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.MerchantRecipe;

import java.util.logging.Level;

public class Profession implements Logger {
    private String name;
    private WeightedTable<TradeGroup> tradegroups = new WeightedTable<>();

    public Profession(ConfigurationSection section) {
        // set the name of the profession
        this.name = section.getName();

        // load trade groups
        ConfigurationSection tradegroupssection = section.getConfigurationSection("groups");
        for(String tradegroupname : tradegroupssection.getKeys(false)) {
            ConfigurationSection tradegroupsection = tradegroupssection.getConfigurationSection(tradegroupname);
            int weight = tradegroupsection.getInt("weight");
            TradeGroup tradegroup = TradeGroup.get(this, tradegroupsection);
            if(tradegroup != null) {
                tradegroups.insert(tradegroup, weight);
            } else {
                log(Level.WARNING, String.format("Profession %s could not load tradegroup %s.", name, tradegroupname));
            }
        }
    }

    public MerchantRecipe recipe(Entity entity) {
        // get trade group
        TradeGroup group = tradegroups.get(entity.random(RandomType.VILLAGER_TRADE));

        // get trade
        Trade trade = group.trade(entity.random(RandomType.VILLAGER_TRADE));

        // return bukkit recipe
        return trade.recipe(entity);
    }

    public String name() { return name; }
}
