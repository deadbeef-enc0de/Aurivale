package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.CooldownTemplate;
import net.mctitan.rpg.modifier.types.CooldownModifier;
import org.bukkit.configuration.ConfigurationSection;

public class CooldownConfig extends ModifierConfig {
    public CooldownConfig(String id) { super(id); }
    public CooldownConfig(String id, ConfigurationSection section) { super(id, section); }

    Operator operator() { return Operator.valueOf(section().getString("operator")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return CooldownModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new CooldownTemplate(rank, base(), monster(), id(), operator(),
                precision(rank), flat(rank), steps(rank));
    }
}
