package net.mctitan.rpg.data.potion;

import net.mctitan.data.BasicData;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.data.status.StatusEffectInstance;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.*;

import java.util.LinkedList;
import java.util.List;

public class PotionInfo extends BasicData {
    private LinkedList<Modifier> modifiers = new LinkedList<>();
    private double area;
    private int duration;
    private int cooldown;
    private double cooldownrecovery;

    public PotionInfo() {}

    public PotionInfo(Entity entity) {
        this(effectmods(entity));
    }

    public PotionInfo(List<Modifier> modifiers) {
        double flatlevel = 0;
        double scalerlevel = 1;
        double morelevel = 1;

        double flatticks = 0;
        double scalerticks = 1;
        double moreticks = 1;

        double flatarea = 0;
        double scalerarea = 1;
        double morearea = 1;

        double duration = 0;
        cooldown = 0;
        cooldownrecovery = 0;

        // go through the modifiers for the potion
        List<Modifier> mods = new LinkedList<>();
        for(Modifier modifier : modifiers) {
            // status modifier
            if(modifier instanceof StatusModifier ||
               modifier instanceof DamageModifier ||
               modifier instanceof HealingModifier) {
                mods.add(modifier);
            }

            // handle change in duration
            else if(modifier instanceof DurationModifier durationmod) {
                switch(durationmod.operator()) {
                    case FLAT -> {
                        if(durationmod.base()) {
                            duration += durationmod.value();
                        } else {
                            flatticks += durationmod.value();
                        }
                    }
                    case SCALER -> { scalerticks += durationmod.value() / 100d; }
                    case MULTIPLIER -> { moreticks *= (1 + durationmod.value() / 100d); }
                }
            }

            // handle change to effect level
            else if(modifier instanceof LevelModifier levelmod) {
                switch(levelmod.operator()) {
                    case FLAT -> { flatlevel += levelmod.value(); }
                    case SCALER -> { scalerlevel += levelmod.value() / 100d; }
                    case MULTIPLIER -> { morelevel *= (1 + levelmod.value() / 100d); }
                }
            }

            // handle area of effect
            else if(modifier instanceof AreaModifier areamod) {
                switch(areamod.operator()) {
                    case FLAT -> { flatarea += areamod.value(); }
                    case SCALER -> { scalerarea += areamod.value() / 100d; }
                    case MULTIPLIER -> { morearea *= (1 + areamod.value() / 100d); }
                }
            }

            // handle cooldown
            else if(modifier instanceof CooldownModifier cooldownmod) {
                switch(cooldownmod.operator()) {
                    case FLAT -> { cooldown += (int)cooldownmod.value(); }
                    case SCALER -> { cooldownrecovery += cooldownmod.value() / 100d; }
                }
            }
        }

        // apply changes to modifiers
        for(Modifier modifier : mods) {
            if(modifier instanceof StatusModifier statusmod) {
                double level = (statusmod.level() + flatlevel) * scalerlevel * morelevel;
                double ticks = (statusmod.ticks() + flatticks) * scalerticks * moreticks;
                this.modifiers.add(statusmod.modify(level, ticks));
            } else {
                this.modifiers.add(modifier);
            }
        }

        // calculate area of effect
        this.area = flatarea * scalerarea * morearea;
        this.duration = (int)(duration * scalerticks * moreticks);;
    }

    private static List<Modifier> effectmods(Entity entity) {
        List<Modifier> modifiers = new LinkedList<>();
        for(StatusEffectInstance instance : entity.statuseffects().effects()) {
            StatusModifier statusmod = StatusModifier.instance(instance);
            modifiers.add(statusmod);
        }

        return modifiers;
    }

    public List<Modifier> modifiers() { return modifiers; }
    public double area() { return area; }
    public int cooldown() { return cooldown; }
    public double cooldownrecovery() { return cooldownrecovery; }

    public int duration() { return Math.max(duration, 5); }

    public void apply(Entity entity) {
        for(Modifier modifier : modifiers) {
            // add status effects instead of applying them
            if(modifier instanceof StatusModifier statusmod) {
                // apply status effect unless entity is immune
                if(!entity.defense().immunities().immune(statusmod.effecttype())) {
                    entity.statuseffects().unapply(statusmod);
                    entity.statuseffects().apply(statusmod);
                }
            }

            // apply damage modifier as damage
            if(modifier instanceof DamageModifier damagemod) {
                DamageRoll damage = entity.defense().apply(damagemod.roll());
                entity.damage(damage.damage());
            }

            // apply everything else
            else {
                modifier.apply(entity);
            }
        }
    }
}
