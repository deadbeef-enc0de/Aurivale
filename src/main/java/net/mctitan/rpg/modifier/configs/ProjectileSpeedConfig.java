package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.enums.ProjectileType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.ProjectileSpeedTemplate;
import net.mctitan.rpg.modifier.types.ProjectileSpeedModifier;
import org.bukkit.configuration.ConfigurationSection;

public class ProjectileSpeedConfig extends ModifierConfig {
    public ProjectileSpeedConfig(String id) {
        super(id);
    }
    public ProjectileSpeedConfig(String id, ConfigurationSection section) { super(id, section); }

    public Operator operator() { return Operator.valueOf(section().getString("operator")); }
    public ProjectileType projectile() { return ProjectileType.valueOf(section().getString("projectile")); }
    public double precision(int rank) { return section().getDouble(String.format("ranks.%d.precision", rank)); }
    public double flat(int rank) { return section().getDouble(String.format("ranks.%d.flat", rank)); }
    public int steps(int rank) { return section().getInt(String.format("ranks.%d.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return ProjectileSpeedModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        return new ProjectileSpeedTemplate(rank, base(), monster(), id(), operator(), projectile(),
                precision(rank), flat(rank), steps(rank));
    }
}
