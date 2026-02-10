package net.mctitan.rpg.data.damage;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.DamageModifier;
import net.mctitan.rpg.modifier.types.DamageRangeModifier;
import net.mctitan.rpg.util.Math;

import java.util.HashMap;
import java.util.Map;

public class DamagePart extends Modable {
    // variables used to calculate minimum and maximum
    private transient double minflat = 0;
    private transient double maxflat = 0;
    private transient double scaler = 1;
    private transient Map<Modifier, Double> modmultis = new HashMap<>();
    private transient Map<StatusEffect, Double> effectmultis = new HashMap<>();

    // damage values
    private double minimum = 0;
    private double maximum = 0;

    public DamagePart() {}

    public DamagePart(double minimum, double maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public DamagePart clone() {
        DamagePart clone = new DamagePart();
        clone.minimum = minimum;
        clone.maximum = maximum;

        return clone;
    }

    public double minimum() { return minimum; }
    public double maximum() { return maximum; }

    public void add(DamagePart part) { add(part.minimum, part.maximum); }
    public void subtract(DamagePart part) { add(-part.minimum, -part.maximum); }

    public void add(double minimum, double maximum) {
        this.minimum += minimum;
        this.maximum += maximum;
    }

    public void addflat(double minimum, double maximum) {
        this.minflat = minimum;
        this.maxflat = maximum;

        calculate();
    }

    public double roll() { return minimum() + (maximum() - minimum()) * Math.nextDouble(); }

    private void calculate() {
        minimum = minflat * scaler;
        maximum = maxflat * scaler;
        for(Double multi : modmultis.values()) {
            minimum *= (1 + multi);
            maximum *= (1 + multi);
        }
        for(Double multi : effectmultis.values()) {
            minimum *= (1 + multi);
            maximum *= (1 + multi);
        }
    }

    @Override
    protected void add(Modifier modifier) {
        // Damage Range Modifier
        if(modifier instanceof DamageRangeModifier damagemod) {
            minflat += damagemod.minimum();
            maxflat += damagemod.maximum();
        }

        // Damage Modifier
        if(modifier instanceof DamageModifier damagemod) {
            switch (damagemod.operator()) {
                case FLAT -> { minflat += damagemod.value(); maxflat += damagemod.value(); }
                case SCALER -> scaler += damagemod.value() / 100d;
                case MULTIPLIER -> modmultis.put(damagemod, damagemod.value() / 100d);
            }
        }

        calculate();
    }

    @Override
    protected void remove(Modifier modifier) {
        // Damage Range Modifier
        if(modifier instanceof DamageRangeModifier damagemod) {
            minflat -= damagemod.minimum();
            maxflat -= damagemod.maximum();
        }

        // Damage Modifier
        if(modifier instanceof DamageModifier damagemod) {
            switch (damagemod.operator()) {
                case FLAT -> { minflat -= damagemod.value(); maxflat -= damagemod.value(); }
                case SCALER -> scaler -= damagemod.value() / 100d;
                case MULTIPLIER -> modmultis.remove(damagemod);
            }
        }

        calculate();
    }

    public void add(StatusEffect effect) {
        switch (effect.effecttype()) {
            case STRENGTH -> effectmultis.put(effect, 0.20d * effect.level());
            case WEAKNESS -> effectmultis.put(effect, -0.15d * effect.level());
        }

        calculate();
    }

    public void remove(StatusEffect effect) {
        effectmultis.remove(effect);
        calculate();
    }
}
