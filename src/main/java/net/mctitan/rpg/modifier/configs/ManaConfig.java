package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.ManaType;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.ManaTemplate;
import net.mctitan.rpg.modifier.types.ManaModifier;
import org.bukkit.configuration.ConfigurationSection;

public class ManaConfig extends ModifierConfig {
    public ManaConfig(String id) {
        super(id);
    }
    public ManaConfig(String id, ConfigurationSection section) { super(id, section); }

    public ManaType manaType() { return ManaType.valueOf(section().getString("manatype")); }
    public Operator operator() { return Operator.valueOf(section().getString("operator")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return ManaModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new ManaTemplate(rank, base(), monster(), id(), manaType(), operator(),
                precision(rank), flat(rank), steps(rank));
    }
}
