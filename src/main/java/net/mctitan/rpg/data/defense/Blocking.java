package net.mctitan.rpg.data.defense;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.BlockDamageModifier;

import java.util.HashMap;
import java.util.Map;

public class Blocking extends Modable {
    // values used to calculate defense value
    private transient double blockedflat = 0;
    private transient double blockedscaler = 1;
    private transient Map<Modifier, Double> blockedmultis = new HashMap<>();

    // blocking value
    private transient double blocked = 0;

    public double blocked() { return blocked; }

    public DamageRoll apply(DamageRoll roll) { return roll.clone().multiply(1 - blocked()); }

    public void calculate() {
        blocked = blockedflat * blockedscaler;
        for(Double multi : blockedmultis.values()) {
            blocked *= (1 + multi);
        }
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof BlockDamageModifier blockmod) {
            switch(blockmod.operator()) {
                case FLAT -> blockedflat += blockmod.value() / 100d;
                case SCALER -> blockedscaler += blockmod.value() / 100d;
                case MULTIPLIER -> blockedmultis.put(modifier, blockmod.value() / 100d);
            }
            calculate();
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof BlockDamageModifier blockmod) {
            switch(blockmod.operator()) {
                case FLAT -> blockedflat -= blockmod.value() / 100d;
                case SCALER -> blockedscaler -= blockmod.value() / 100d;
                case MULTIPLIER -> blockedmultis.remove(modifier);
            }
            calculate();
        }
    }
}
