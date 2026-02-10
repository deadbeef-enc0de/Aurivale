package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.ReflectDamageTemplate;
import net.mctitan.rpg.modifier.types.ReflectDamageModifier;
import org.bukkit.configuration.ConfigurationSection;

public class ReflectDamageConfig extends ModifierConfig {
    public ReflectDamageConfig(String id) {
        super(id);
    }
    public ReflectDamageConfig(String id, ConfigurationSection section) { super(id, section); }

    public DamageType damagetype() { return DamageType.valueOf(section().getString("damagetype")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return ReflectDamageModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new ReflectDamageTemplate(rank, base(), monster(), id(), damagetype(),
                precision(rank), flat(rank), steps(rank));
    }
}
