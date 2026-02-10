package net.mctitan.rpg.data.action.spell;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.AreaModifier;

import java.util.HashMap;
import java.util.Map;

public class Area extends Modable {
    private double radiusflat = 0;
    private double radiusscaler = 1;
    private Map<Modifier, Double> radiusmultis = new HashMap<>();

    public double value(double original) {
        double ret = (original + radiusflat) * radiusscaler;
        for(Double multi : radiusmultis.values()) {
            ret *= (1 + multi);
        }

        return ret;
    }

    public double scaling() {
        double ret = radiusscaler;
        for(Double multi : radiusmultis.values()) {
            ret *= (1 + multi);
        }

        return ret;
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof AreaModifier areamod) {
            switch(areamod.operator()) {
                case FLAT -> { radiusflat += areamod.value(); }
                case SCALER -> { radiusscaler += areamod.value() / 100; }
                case MULTIPLIER ->  { radiusmultis.put(areamod, areamod.value() / 100); }
            }
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof AreaModifier areamod) {
            switch(areamod.operator()) {
                case FLAT -> { radiusflat -= areamod.value(); }
                case SCALER -> { radiusscaler -= areamod.value() / 100; }
                case MULTIPLIER ->  { radiusmultis.remove(areamod); }
            }
        }
    }
}
