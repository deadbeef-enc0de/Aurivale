package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.DamageRangeTemplate;
import net.mctitan.rpg.modifier.types.DamageRangeModifier;
import org.bukkit.configuration.ConfigurationSection;

public class DamageRangeConfig extends ModifierConfig {
    public DamageRangeConfig(String id) { super(id); }
    public DamageRangeConfig(String id, ConfigurationSection section) { super(id, section); }

    public DamageType damagetype() { return DamageType.valueOf(section().getString("damagetype")); }

    public double minimumprecision(int rank) { return section().getDouble(String.format("ranks.%d.minimum.precision", rank)); }
    public double minimumflat(int rank) { return section().getDouble(String.format("ranks.%d.minimum.flat", rank)); }
    public int minimumsteps(int rank) { return section().getInt(String.format("ranks.%d.minimum.steps", rank)); }

    public double maximumprecision(int rank) { return section().getDouble(String.format("ranks.%d.maximum.precision", rank)); }
    public double maximumflat(int rank) { return section().getDouble(String.format("ranks.%d.maximum.flat", rank)); }
    public int maximumsteps(int rank) { return section().getInt(String.format("ranks.%d.maximum.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return DamageRangeModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new DamageRangeTemplate(rank, base(), monster(), id(), damagetype(),
                minimumprecision(rank), minimumflat(rank), minimumsteps(rank),
                maximumprecision(rank), maximumflat(rank), maximumsteps(rank)
        );
    }
}
