package net.mctitan.rpg.util.comparators;

import net.mctitan.rpg.modifier.types.EnchantmentModifier;

import java.util.Comparator;

public class EnchantmentModifierComparator implements Comparator<EnchantmentModifier> {
    public int compare(EnchantmentModifier a, EnchantmentModifier b) {
        if(a.value() != b.value()) {
            return b.value() > a.value() ? 1 : -1;
        }
        return a.hashCode() - b.hashCode();
    }
}
