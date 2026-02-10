package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.Attribute;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.AttributeTemplate;
import net.mctitan.rpg.modifier.types.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;

public class AttributeConfig extends ModifierConfig {
    public AttributeConfig(String id) {
        super(id);
    }
    public AttributeConfig(String id, ConfigurationSection section) { super(id, section); }

    public Attribute attribute() { return Attribute.valueOf(section().getString("attribute")); }
    public Operator operator() { return Operator.valueOf(section().getString("operator")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%s.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return AttributeModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new AttributeTemplate(rank, base(), monster(), id(), attribute(), operator(),
                precision(rank), flat(rank), steps(rank));
    }
}
