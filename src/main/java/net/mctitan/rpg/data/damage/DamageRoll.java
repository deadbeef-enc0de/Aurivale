package net.mctitan.rpg.data.damage;

import net.mctitan.data.BasicData;
import net.mctitan.rpg.enums.DamageType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DamageRoll extends BasicData {
    private HashMap<DamageType, Double> damages = new HashMap<>();

    public DamageRoll clone() {
        DamageRoll roll = new DamageRoll();
        for(Map.Entry<DamageType, Double> entry : damages.entrySet()) {
            roll.set(entry.getKey(), entry.getValue());
        }

        return roll;
    }

    public Set<DamageType> damagetypes() { return new HashSet<>(damages.keySet()); }

    public double damage(DamageType damagetype) {
        if(damages.containsKey(damagetype)) {
            return damages.get(damagetype);
        }
        return 0;
    }

    public double damage() {
        double ret = 0;
        for(DamageType damagetype : damagetypes()) {
            double damage = damage(damagetype);
            if(damage <= 0) { continue; }
            ret += damage;
        }

        return ret;
    }

    public void add(DamageRoll roll) {
        for(Map.Entry<DamageType, Double> entry : roll.damages.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public void add(DamageType type, DamagePart part) {
        double damage = part.roll();
        if(damage <= 0) { return; }

        if(!damages.containsKey(type)) { damages.put(type, 0d); }
        damages.put(type, damages.get(type) + damage);
    }

    public void add(DamageType type, double damage) {
        if(!damages.containsKey(type)) { damages.put(type, 0d); }
        damages.put(type, damages.get(type) + damage);
        if(damages.get(type) == 0) { damages.remove(type); }
    }

    public void remove(DamageType type, double damage) { add(type, -damage); }

    public DamageRoll multiply(double multiplier) {
        for(DamageType type : damagetypes()) {
            multiply(type, multiplier);
        }

        return this;
    }

    public DamageRoll multiply(DamageType type, double mulitplier) {
        damages.put(type, damages.get(type) * mulitplier);
        return this;
    }

    public void set(DamageType type, double damage) { damages.put(type, damage); }
}
