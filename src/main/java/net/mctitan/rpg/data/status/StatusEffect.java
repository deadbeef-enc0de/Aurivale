package net.mctitan.rpg.data.status;

import net.mctitan.data.BasicData;
import net.mctitan.data.UUID;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.EffectType;
import org.bukkit.potion.PotionEffect;

import java.util.Objects;

public class StatusEffect extends BasicData {
    private EffectType effecttype;
    private int level;
    private boolean permanent;
    private int duration;
    private UUID uuid = new UUID();

    public StatusEffect() {} // for YamlSaver

    public StatusEffect(EffectType effecttype, int level, boolean permanent) { this(effecttype, level, permanent, -1); }
    public StatusEffect(EffectType effecttype, int level, int duration) { this(effecttype, level, false, duration); }
    private StatusEffect(EffectType effecttype, int level, boolean permanent, int duration) {
        this.effecttype = effecttype;
        this.level = level;
        this.permanent = permanent;
        this.duration = duration;
    }

    public StatusEffect(PotionEffect effect) {
        this.effecttype = EffectType.effecttype(effect.getType());
        this.level = effect.getAmplifier() + 1;
        this.permanent = effect.getDuration() == PotionEffect.INFINITE_DURATION;
        this.duration = effect.getDuration();
    }

    public EffectType effecttype() { return effecttype; }
    public int level() { return level; }
    public boolean permanent() { return permanent; }
    public int duration() { return duration; }

    public StatusEffectInstance instance(Entity entity) {
        return new StatusEffectInstance(this, entity);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof StatusEffect other)) { return false; }
        return this.effecttype == other.effecttype &&
               this.level == other.level &&
               this.permanent == other.permanent &&
               this.duration == other.duration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(effecttype, level, permanent, duration, uuid);
    }
}
