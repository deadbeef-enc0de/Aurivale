package net.mctitan.rpg.trading.trades;

import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.trading.Trade;
import net.mctitan.rpg.trading.TradeInstance;
import net.mctitan.rpg.trading.Trading;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.Math;
import org.bukkit.Material;

import java.util.List;
import java.util.Random;

public class ItemTrade extends Trade implements Logger {
    private Material material;

    public ItemTrade(Material material) { this.material = material; }

    @Override
    public TradeInstance instance(Random random, int luck) {
        // get item stack to be sold
        int amount = Math.nextInt(1, material.getMaxStackSize());
        ItemStack stack = new ItemStack(material, amount);

        // get cost of stack
        int value = amount * Trading.instance().value(material);
        List <ItemStack> cost;
        if(Trading.instance().materialcurrency().contains(material)) {
            cost = Trading.instance().enchanterstacks(value);
        } else {
            cost = Trading.instance().materialstacks(value);
        }
        
        return new TradeInstance(value, stack, cost);
    }
}
