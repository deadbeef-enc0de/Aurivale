package net.mctitan.rpg.util.comparators;

import org.bukkit.NamespacedKey;

import java.util.Comparator;

public class KeySlotComparator implements Comparator<NamespacedKey> {
    public int compare(NamespacedKey a, NamespacedKey b) {
        int slota = Integer.parseInt(a.getKey());
        int slotb = Integer.parseInt(b.getKey());
        return slota - slotb;
    }
}
