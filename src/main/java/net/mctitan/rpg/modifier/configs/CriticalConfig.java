package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.CriticalType;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.CriticalTemplate;
import net.mctitan.rpg.modifier.types.CriticalModifier;
import org.bukkit.configuration.ConfigurationSection;

public class CriticalConfig extends ModifierConfig {
    public CriticalConfig(String id) {
        super(id);
    }
    public CriticalConfig(String id, ConfigurationSection section) { super(id, section); }

    CriticalType criticaltype() { return CriticalType.valueOf(section().getString("criticaltype")); }
    Operator operator() { return Operator.valueOf(section().getString("operator")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return CriticalModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new CriticalTemplate(rank, base(), monster(), id(), criticaltype(), operator(),
                precision(rank), flat(rank), steps(rank));
    }
}
