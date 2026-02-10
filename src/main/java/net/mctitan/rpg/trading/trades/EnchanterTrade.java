package net.mctitan.rpg.trading.trades;

import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.trading.Trade;
import net.mctitan.rpg.trading.TradeInstance;
import net.mctitan.rpg.trading.Trading;
import net.mctitan.rpg.util.Logger;

import java.util.List;
import java.util.Random;

public class EnchanterTrade extends Trade implements Logger {
    private EnchanterType enchantertype;

    public EnchanterTrade(EnchanterType enchantertype) { this.enchantertype = enchantertype; }

    @Override
    public TradeInstance instance(Random random, int luck) {
        // get the result stack
        ItemStack stack = enchantertype.enchanter().stack();

        // get the cost and instance
        int value = Trading.instance().value(enchantertype);
        List<ItemStack> cost = Trading.instance().materialstacks(value);
        return new TradeInstance(value, stack, cost);
    }
}
