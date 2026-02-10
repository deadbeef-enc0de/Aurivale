package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.DamageReductionMaxTemplate;
import net.mctitan.rpg.modifier.types.DamageReductionMaxModifier;
import org.bukkit.configuration.ConfigurationSection;

public class DamageReductionMaxConfig extends ModifierConfig {
    public DamageReductionMaxConfig(String id) {
        super(id);
    }
    public DamageReductionMaxConfig(String id, ConfigurationSection section) { super(id, section); }

    public DamageType damagetype() { return DamageType.valueOf(section().getString("damagetype")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return DamageReductionMaxModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new DamageReductionMaxTemplate(rank, base(), monster(), id(), damagetype(),
                precision(rank), flat(rank), steps(rank));
    }
}
