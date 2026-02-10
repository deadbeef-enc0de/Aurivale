package net.mctitan.rpg.data.action.attack;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.LeechModifier;

import java.util.HashMap;

public class Leech extends Modable {
    private HashMap<DamageType, Double> lifeleech = new HashMap<>();

    public Leech() {}

    public Leech(Leech other) {
        for(DamageType damagetype : other.lifeleech.keySet()) {
            lifeleech.put(damagetype, other.leech(damagetype));
        }
    }

    public Leech clone() { return new Leech(this); }

    public double lifeleech(DamageRoll roll) {
        // clone damage roll to not change it
        DamageRoll leechroll = roll.clone();

        // apply leech to the damage types in the damage
        for(DamageType damagetype : leechroll.damagetypes()) {
            double damage = leechroll.damage(damagetype);
            leechroll.multiply(damagetype, leech(damagetype));
        }

        // roll damage is the amount of leech
        return leechroll.damage();
    }

    private double leech(DamageType damagetype) {
        if(lifeleech.containsKey(damagetype)) {
            return lifeleech.get(damagetype);
        }
        return 0;
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof LeechModifier leechmod) {
            lifeleech.put(leechmod.damagetype(), leech(leechmod.damagetype()) + leechmod.value() / 100d);
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof LeechModifier leechmod) {
            lifeleech.put(leechmod.damagetype(), leech(leechmod.damagetype()) - leechmod.value() / 100d);
            if(leech(leechmod.damagetype()) <= 0) {
                lifeleech.remove(leechmod.damagetype());
            }
        }
    }
}
