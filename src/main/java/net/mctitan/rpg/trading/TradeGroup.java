package net.mctitan.rpg.trading;

import net.mctitan.rpg.trading.groups.CraftableTradeGroup;
import net.mctitan.rpg.trading.groups.EnchanterTradeGroup;
import net.mctitan.rpg.trading.groups.ItemTradeGroup;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Random;

public abstract class TradeGroup {
    private Profession profession;
    private String name;
    private int weight;

    private ArrayList<Trade> trades = new ArrayList<>();

    public TradeGroup(Profession profession, String name, int weight) {
        this.profession = profession;
        this.name = name;
        this.weight = weight;
    }

    public Profession profession() { return profession; }
    public String name() { return name; }
    public int weight() { return weight; }

    public Trade trade(Random random) { return trades.get(random.nextInt(trades.size())); }

    protected void add(Trade trade) { trades.add(trade); }

    public static TradeGroup get(Profession profession, ConfigurationSection section) {
        String name = section.getName();
        int weight = section.getInt("weight");

        if(section.contains("craftables")) {
            return new CraftableTradeGroup(profession, name, weight, section);
        } else if(section.contains("enchanters")) {
            return new EnchanterTradeGroup(profession, name, weight, section);
        } else if(section.contains("items")) {
            return new ItemTradeGroup(profession, name, weight, section);
        }

        return null;
    }
}
