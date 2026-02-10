package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.EnchantmentTemplate;
import net.mctitan.rpg.modifier.types.EnchantmentModifier;
import org.bukkit.configuration.ConfigurationSection;

public class EnchantmentConfig extends ModifierConfig {
    public EnchantmentConfig(String id) {
        super(id);
    }
    public EnchantmentConfig(String id, ConfigurationSection section) { super(id, section); }

    Enchantment enchantment() { return Enchantment.valueOf(section().getString("enchantment")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank), 1); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return EnchantmentModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new EnchantmentTemplate(rank, base(), monster(), id(), enchantment(),
                precision(rank), flat(rank), steps(rank));
    }
}
