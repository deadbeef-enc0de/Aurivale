package net.mctitan.rpg.util.comparators;

import net.mctitan.rpg.data.ItemStack;

import java.util.Comparator;

public class ItemStackComparator implements Comparator<ItemStack> {
    public int compare(ItemStack a, ItemStack b) {
        int acount = a.bukkitstack().getAmount();
        int bcount = b.bukkitstack().getAmount();
        if(acount != bcount) { return acount - bcount; }

        int acode = a.bukkitstack().getType().ordinal();
        int bcode = b.bukkitstack().getType().ordinal();
        if(acode != bcode) { return acode - bcode; }

        return a.hashCode() - b.hashCode();
    }
}
