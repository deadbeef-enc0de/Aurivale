package net.mctitan.rpg.data.action.attack;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.AttributeModifier;

import java.util.HashMap;
import java.util.Map;

public class AttackTime extends Modable {
    private static final double SWING_POWER = 2;

    // variables used to calculate attack time
    private transient double base = 0;
    private transient double scaler = 1;
    private transient Map<Modifier, Double> multis = new HashMap<>();
    private transient double haste = 0;
    private transient double fatique = 0;

    // actual attack time
    private transient double time = 0;
    private transient int lastswing = 0;
    private transient double lastmulti = 0;

    public double time() { return time; }

    public void reset(Entity entity) {
        lastswing = entity.bukkitentity().getTicksLived();
        lastmulti = 0;
    }

    public double swing(Entity entity) {
        if(time <= 0) {
            return 0;
        }

        int swingtick = entity.bukkitentity().getTicksLived();
        if(lastswing == swingtick) {
            return lastmulti;
        }

        // get ticks since swing, set lastswing to now
        double ticks = Math.min(time, swingtick - lastswing);
        lastswing = swingtick;
        lastmulti = Math.pow(ticks / time, SWING_POWER);

        return lastmulti;
    }

    private void calculate() {
        time = base * (1 + 0.1 * fatique) / scaler / (1 + 0.1 * haste);
        for(double multi : multis.values()) {
            time *= (1 + multi);
        }
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof AttributeModifier attributemod) {
            switch(attributemod.attribute()) {
                case ATTACK_SPEED -> {
                    switch(attributemod.operator()) {
                        case FLAT -> base += 20 * attributemod.value();
                        case SCALER -> scaler += attributemod.value() / 100d;
                        case MULTIPLIER -> multis.put(modifier, attributemod.value() / 100d);
                    }
                }
            }
        }

        calculate();
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof AttributeModifier attributemod) {
            switch(attributemod.attribute()) {
                case ATTACK_SPEED -> {
                    switch(attributemod.operator()) {
                        case FLAT -> base -= 20 * attributemod.value();
                        case SCALER -> scaler -= attributemod.value() / 100d;
                        case MULTIPLIER -> multis.remove(modifier);
                    }
                }
            }
        }

        calculate();
    }

    public void add(StatusEffect effect) {
        switch(effect.effecttype()) {
            case HASTE -> haste += effect.level();
            case MINING_FATIGUE -> fatique += effect.level();
        }

        calculate();
    }

    public void remove(StatusEffect effect) {
        switch(effect.effecttype()) {
            case HASTE -> haste -= effect.level();
            case MINING_FATIGUE -> fatique -= effect.level();
        }

        calculate();
    }
}
