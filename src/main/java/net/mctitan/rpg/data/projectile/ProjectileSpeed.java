package net.mctitan.rpg.data.projectile;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.ProjectileSpeedModifier;

import java.util.HashMap;
import java.util.Map;

public class ProjectileSpeed extends Modable {
    // variables to calculate speed
    private transient double speedbase = 0;
    private transient double speedscaler = 1;
    private transient Map<Modifier, Double> speedmultis = new HashMap<>();

    // arrow speed
    private transient double value;

    public double value() { return value; }
    public double value(double flat) {
        double ret = flat * speedscaler;
        for(Double multi : speedmultis.values()) {
            ret *= (1 + multi);
        }

        return ret;
    }

    private void calculate() {
        value = speedbase * speedscaler;
        for(Double multi : speedmultis.values()) {
            value *= (1 + multi);
        }
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof ProjectileSpeedModifier projmod) {
            switch(projmod.operator()) {
                case FLAT -> speedbase += projmod.value();
                case SCALER -> speedscaler += projmod.value() / 100d;
                case MULTIPLIER -> speedmultis.put(modifier, projmod.value() / 100d);
            }
        }

        calculate();
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof ProjectileSpeedModifier projmod) {
            switch(projmod.operator()) {
                case FLAT -> speedbase -= projmod.value();
                case SCALER -> speedscaler -= projmod.value() / 100d;
                case MULTIPLIER -> speedmultis.remove(modifier);
            }
        }

        calculate();
    }
}
