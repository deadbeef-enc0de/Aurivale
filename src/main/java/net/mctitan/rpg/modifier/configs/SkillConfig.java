package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.enums.SkillType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.SkillTemplate;
import net.mctitan.rpg.modifier.types.SkillModifier;
import org.bukkit.configuration.ConfigurationSection;

public class SkillConfig extends ModifierConfig {
    public SkillConfig(String id) {
        super(id);
    }
    public SkillConfig(String id, ConfigurationSection section) { super(id, section); }

    public SkillType skill() { return SkillType.valueOf(section().getString("skill")); }
    public Operator operator() { return Operator.valueOf(section().getString("operator")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return SkillModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new SkillTemplate(rank, base(), monster(), id(), skill(), operator(),
                precision(rank), flat(rank), steps(rank)
        );
    }
}
