package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.AreaTemplate;
import net.mctitan.rpg.modifier.types.DurationModifier;
import org.bukkit.configuration.ConfigurationSection;

public class AreaConfig extends ModifierConfig {
    public AreaConfig(String id) { super(id); }
    public AreaConfig(String id, ConfigurationSection section) { super(id, section); }

    Operator operator() { return Operator.valueOf(section().getString("operator")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return DurationModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new AreaTemplate(rank, base(), monster(), id(), operator(),
                precision(rank), flat(rank), steps(rank));
    }
}
