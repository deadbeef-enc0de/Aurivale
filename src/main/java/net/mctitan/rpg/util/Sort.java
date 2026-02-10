package net.mctitan.rpg.util;

import net.mctitan.rpg.util.comparators.KeySlotComparator;
import org.bukkit.NamespacedKey;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class Sort {
    public static Set<NamespacedKey> slot(Collection<NamespacedKey> keys) {
        Set<NamespacedKey> ret = new TreeSet<>(new KeySlotComparator());
        ret.addAll(keys);
        return ret;
    }
}
