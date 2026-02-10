package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.SpellActivation;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.SpellTemplate;
import net.mctitan.rpg.modifier.types.SpellModifier;
import org.bukkit.configuration.ConfigurationSection;

public class SpellConfig extends ModifierConfig {
    public SpellConfig(String id) { super(id); }
    public SpellConfig(String id, ConfigurationSection section) { super(id, section); }

    public SpellType spelltype() { return SpellType.valueOf(section().getString("spelltype")); }
    public SpellActivation activation() { return SpellActivation.valueOf(section().getString("activation")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return SpellModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new SpellTemplate(rank, base(), monster(), id(), spelltype(), activation(),
                precision(rank), flat(rank), steps(rank));
    }
}
