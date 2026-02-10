package net.mctitan.rpg.data.enchantment;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.EnchantmentModifier;
import net.mctitan.rpg.util.comparators.EnchantmentModifierComparator;

import java.util.HashMap;
import java.util.TreeSet;

public class Enchantments extends Modable {
    private HashMap<Enchantment, TreeSet<EnchantmentModifier>> enchantments = new HashMap<>();

    public int level(Enchantment enchantment) {
        if(enchantments.containsKey(enchantment) && !enchantments.get(enchantment).isEmpty()) {
            return (int)enchantments.get(enchantment).getFirst().value();
        }
        return 0;
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof EnchantmentModifier enchantmod) {
            if(!enchantments.containsKey(enchantmod.enchantment())) {
                enchantments.put(enchantmod.enchantment(), new TreeSet<>(new EnchantmentModifierComparator()));
            }
            enchantments.get(enchantmod.enchantment()).add(enchantmod);
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof EnchantmentModifier enchantmod) {
            if(!enchantments.containsKey(enchantmod.enchantment())) {
                return;
            }
            enchantments.get(enchantmod.enchantment()).remove(enchantmod);
            if(enchantments.get(enchantmod.enchantment()).isEmpty()) {
                enchantments.remove(enchantmod.enchantment());
            }
        }
    }
}
