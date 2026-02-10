package net.mctitan.rpg.trading;

import net.mctitan.rpg.data.ItemStack;

import java.util.List;

public class TradeInstance {
    private int value = -1;
    private ItemStack item;
    private List<ItemStack> cost;

    public TradeInstance(int value, ItemStack item, List<ItemStack> cost) {
        this.value = value;
        this.item = item;
        this.cost = cost;
    }

    public int value() { return value; }
    public ItemStack item() { return item; }
    public List<ItemStack> cost() { return cost; }
}
