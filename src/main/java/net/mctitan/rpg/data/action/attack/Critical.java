package net.mctitan.rpg.data.action.attack;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.CriticalModifier;
import net.mctitan.rpg.util.Math;

import java.util.HashMap;
import java.util.Map;

public class Critical extends Modable {
    // variables used to calculate chance
    private transient double chanceflat = 0;
    private transient double chancescaler = 1;
    private transient Map<Modifier, Double> chancemultis = new HashMap<>();

    // variables used to calculate damage
    private transient double damageflat = 0;
    private transient double damagescaler = 1;
    private transient Map<Modifier, Double> damagemultis = new HashMap<>();

    // critical values
    private double chance = 0;
    private double damage = 0;

    public Critical clone() {
        Critical clone = new Critical();
        clone.chance = chance;
        clone.damage = damage;

        return clone;
    }

    public boolean iscritical() { return Math.nextDouble() < chance; }
    public double chance() { return chance; }
    public double damage() { return damage; }

    private void chancecalc() {
        chance = chanceflat * chancescaler;
        for(double multi : chancemultis.values()) {
            chance *= (1 + multi);
        }
    }

    private void damagecalc() {
        damage = damageflat * damagescaler;
        for(double multi : damagemultis.values()) {
            damage *= (1 + multi);
        }
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof CriticalModifier critmod) {
            switch(critmod.criticaltype()) {
                case CRITICAL_CHANCE -> {
                    switch(critmod.operator()) {
                        case FLAT -> { chanceflat += critmod.value() / 100d;  }
                        case SCALER -> { chancescaler += critmod.value() / 100d;  }
                        case MULTIPLIER -> { chancemultis.put(modifier, critmod.value() / 100d); }
                    }
                    chancecalc();
                }
                case CRITICAL_DAMAGE -> {
                    switch(critmod.operator()) {
                        case FLAT -> { damageflat += critmod.value() / 100d;  }
                        case SCALER -> { damagescaler += critmod.value() / 100d;  }
                        case MULTIPLIER -> { damagemultis.put(modifier, critmod.value() / 100d);  }
                    }
                    damagecalc();
                }
            }
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof CriticalModifier critmod) {
            switch(critmod.criticaltype()) {
                case CRITICAL_CHANCE -> {
                    switch(critmod.operator()) {
                        case FLAT -> { chanceflat -= critmod.value() / 100d;  }
                        case SCALER -> { chancescaler -= critmod.value() / 100d;  }
                        case MULTIPLIER -> { chancemultis.remove(modifier); }
                    }
                    chancecalc();
                }
                case CRITICAL_DAMAGE -> {
                    switch(critmod.operator()) {
                        case FLAT -> { damageflat -= critmod.value() / 100d;  }
                        case SCALER -> { damagescaler -= critmod.value() / 100d;  }
                        case MULTIPLIER -> { damagemultis.remove(modifier);  }
                    }
                    damagecalc();
                }
            }
        }
    }
}
