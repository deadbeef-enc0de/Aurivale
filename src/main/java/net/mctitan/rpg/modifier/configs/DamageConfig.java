package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.DamageTemplate;
import net.mctitan.rpg.modifier.types.DamageModifier;
import org.bukkit.configuration.ConfigurationSection;

public class DamageConfig extends ModifierConfig {
    public DamageConfig(String id) {
        super(id);
    }
    public DamageConfig(String id, ConfigurationSection section) { super(id, section); }

    public DamageType damagetype() { return DamageType.valueOf(section().getString("damagetype")); }
    Operator operator() { return Operator.valueOf(section().getString("operator")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return DamageModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new DamageTemplate(rank, base(), monster(), id(), damagetype(), operator(),
                precision(rank), flat(rank), steps(rank));
    }
}
