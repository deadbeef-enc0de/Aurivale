package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.Conversion;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.DamageConversionTemplate;
import net.mctitan.rpg.modifier.types.DamageConversionModifier;
import org.bukkit.configuration.ConfigurationSection;

public class DamageConversionConfig extends ModifierConfig {
    public DamageConversionConfig(String id) {
        super(id);
    }
    public DamageConversionConfig(String id, ConfigurationSection section) { super(id, section); }

    public Conversion conversion() { return Conversion.valueOf(section().getString("conversion")); }
    public DamageType fromtype() { return DamageType.valueOf(section().getString("fromtype")); }
    public DamageType totype() { return DamageType.valueOf(section().getString("totype")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return DamageConversionModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new DamageConversionTemplate(rank, base(), monster(), id(),
                conversion(), fromtype(), totype(),
                precision(rank), flat(rank), steps(rank));
    }
}
