package net.mctitan.rpg.data.defense;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.DamageReductionMaxModifier;
import net.mctitan.rpg.modifier.types.DamageTakenModifier;
import net.mctitan.rpg.modifier.types.DefenseModifier;

import java.util.HashMap;
import java.util.Map;

public class Reduction extends Modable {
    private static final double DAMAGE_MULTI = 3d;
    private static final double DEFENSE_FLAT_DR_DIVISOR = 20d;
    private static final double NEGATIVE_DEFENSE_DIVISOR = 50d;

    // values used to calculate defense value
    private transient double base = 0;
    private transient double scaler = 1;
    private transient Map<Modifier, Double> multis = new HashMap<>();
    private transient int immunities = 0;

    // reducton values
    private transient double flatmod = 0;
    private transient double defense = 0;
    private transient double flat = 0;
    private transient double flatdr = 0;
    private transient double maximum = 0;

    public int defense() { return (int)defense; }
    public double flat() { return flat; }

    public double apply(double damage) {
        damage += flat;
        if(damage <= 0) { return 0; }

        return damage * (1 - dr(damage));
    }

    public double dr(double damage) {
        if(damage <= 0 || immunities > 0) { return 1; }

        double dr;
        if(defense < 0) {
            dr = defense / NEGATIVE_DEFENSE_DIVISOR;
        } else {
            dr = Math.min(maximum, defense / (defense + DAMAGE_MULTI * damage) - flatdr);
        }

        return dr;
    }

    private void calculate() {
        defense = base * scaler;
        for (Double multi : multis.values()) {
            defense *= (1 + multi);
        }
        defense = (int)defense;

        flat = flatmod - Math.max(0, defense / DEFENSE_FLAT_DR_DIVISOR);
    }

    @Override
    protected void add(Modifier modifier) {
        // Defense Modifier
        if(modifier instanceof DefenseModifier defensemod) {
            switch (defensemod.operator()) {
                case FLAT -> base += defensemod.value();
                case SCALER -> scaler += defensemod.value() / 100d;
                case MULTIPLIER -> multis.put(modifier, defensemod.value() / 100d);
            }
            calculate();

        // Damage Taken Modifier
        } else if(modifier instanceof DamageTakenModifier damagetakenmod) {
            switch (damagetakenmod.operator()) {
                case FLAT -> flatmod += damagetakenmod.value();
                case SCALER -> flatdr += damagetakenmod.value() / 100d;
            }

        // Damage Reduction Maximum Modifier
        } else if(modifier instanceof DamageReductionMaxModifier maxreductionmod) {
            maximum += maxreductionmod.value() / 100d;
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        // Defense Modifier
        if(modifier instanceof DefenseModifier defensemod) {
            switch (defensemod.operator()) {
                case FLAT -> base -= defensemod.value();
                case SCALER -> scaler -= defensemod.value() / 100d;
                case MULTIPLIER -> multis.remove(modifier);
            }
            calculate();

        // Damage Taken Modifier
        } else if(modifier instanceof DamageTakenModifier damagetakenmod) {
            switch (damagetakenmod.operator()) {
                case FLAT -> flatmod -= damagetakenmod.value();
                case SCALER -> flatdr -= damagetakenmod.value() / 100d;
            }

        // Damage Reduction Maximum Modifier
        } else if(modifier instanceof DamageReductionMaxModifier maxreductionmod) {
            maximum -= maxreductionmod.value() / 100d;
        }
    }
}
