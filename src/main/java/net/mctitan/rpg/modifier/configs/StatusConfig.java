package net.mctitan.rpg.modifier.configs;

import net.mctitan.rpg.enums.EffectType;
import net.mctitan.rpg.enums.StatusType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.StatusTemplate;
import net.mctitan.rpg.modifier.types.StatusModifier;
import org.bukkit.configuration.ConfigurationSection;

public class StatusConfig extends ModifierConfig {
    public StatusConfig(String id) {
        super(id);
    }
    public StatusConfig(String id, ConfigurationSection section) { super(id, section); }

    public StatusType statustype() { return StatusType.valueOf(section().getString("statustype")); }
    public EffectType effecttype() { return EffectType.valueOf(section().getString("effecttype")); }

    public EffectType effecttype(int rank) { return EffectType.valueOf(section().getString(String.format("ranks.%d.effecttype", rank))); }

    public double levelprecision(int rank) { return section().getDouble(String.format("ranks.%d.level.precision", rank)); }
    public double levelflat(int rank) { return section().getDouble(String.format("ranks.%d.level.flat", rank)); }
    public int levelsteps(int rank) { return section().getInt(String.format("ranks.%d.level.steps", rank)); }

    public double ticksprecision(int rank) { return section().getDouble(String.format("ranks.%d.ticks.precision", rank)); }
    public double ticksflat(int rank) { return section().getDouble(String.format("ranks.%d.ticks.flat", rank)); }
    public int tickssteps(int rank) { return section().getInt(String.format("ranks.%d.ticks.steps", rank)); }

    public double radiusprecision(int rank) { return section().getDouble(String.format("ranks.%d.radius.precision", rank)); }
    public double radiusflat(int rank) { return section().getDouble(String.format("ranks.%d.radius.flat", rank)); }
    public int radiussteps(int rank) { return section().getInt(String.format("ranks.%d.radius.steps", rank)); }

    @Override
    public Class<? extends Modifier> modifierclass() { return StatusModifier.class; }

    @Override
    public ModifierTemplate template(int rank) {
        if(statustype() == StatusType.PERMANENT) {
            return StatusTemplate.permanent(rank, base(), monster(), id(), effecttype(),
                    levelprecision(rank), levelflat(rank), levelsteps(rank));
        } else if(statustype() == StatusType.TEMPORARY) {
            return StatusTemplate.temporary(rank, base(), monster(), id(), effecttype(rank),
                    levelprecision(rank), levelflat(rank), levelsteps(rank),
                    ticksprecision(rank), ticksflat(rank), tickssteps(rank));
        } else if(statustype() == StatusType.IMMUNITY) {
            return StatusTemplate.immunity(rank, base(), monster(), id(), effecttype());
        } else if(statustype() == StatusType.AURA) {
            return StatusTemplate.aura(rank, base(), monster(), id(), effecttype(rank),
                    levelprecision(rank), levelflat(rank), levelsteps(rank),
                    radiusprecision(rank), radiusflat(rank), radiussteps(rank));
        }

        return null;
    }
}
