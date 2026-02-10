package net.mctitan.rpg.data.action.spell;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.DurationModifier;

import java.util.HashMap;
import java.util.Map;

public class Duration extends Modable {
    private double durationflat = 0;
    private double durationscaler = 1;
    private Map<Modifier, Double> durationmultis = new HashMap<>();

    public int value(int original) {
        double ret = (original + durationflat) * durationscaler;
        for(Double multi : durationmultis.values()) {
            ret *= (1 + multi);
        }

        return (int)ret;
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof DurationModifier durationmod) {
            switch(durationmod.operator()) {
                case FLAT -> { durationflat += durationmod.value(); }
                case SCALER -> { durationscaler += durationmod.value() / 100; }
                case MULTIPLIER ->  { durationmultis.put(durationmod, durationmod.value() / 100); }
            }
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof DurationModifier durationmod) {
            switch(durationmod.operator()) {
                case FLAT -> { durationflat -= durationmod.value(); }
                case SCALER -> { durationscaler -= durationmod.value() / 100; }
                case MULTIPLIER ->  { durationmultis.remove(durationmod); }
            }
        }
    }
}
