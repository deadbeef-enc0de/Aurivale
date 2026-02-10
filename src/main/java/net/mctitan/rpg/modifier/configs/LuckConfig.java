package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.LuckType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.LuckTemplate;
import net.mctitan.rpg.modifier.types.LuckModifier;
import org.bukkit.configuration.ConfigurationSection;

public class LuckConfig extends ModifierConfig {
    public LuckConfig(String id) {
        super(id);
    }
    public LuckConfig(String id, ConfigurationSection section) { super(id, section); }

    public LuckType lucktype() { return LuckType.valueOf(section().getString("lucktype")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return LuckModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new LuckTemplate(rank, base(), monster(), id(), lucktype(),
                precision(rank), flat(rank), steps(rank));
    }
}
