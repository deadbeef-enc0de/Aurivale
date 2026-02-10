package net.mctitan.rpg.data.defense;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.ReflectDamageModifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DamageReflection extends Modable {
    private transient Map<DamageType, Double> reflections = new HashMap<>();

    public DamageRoll reflect(DamageRoll damage) {
        DamageRoll ret = damage.clone();
        for(DamageType damagetype : ret.damagetypes()) {
            if(reflections.containsKey(damagetype)) {
                ret.multiply(damagetype, reflections.get(damagetype));
            } else {
                ret.set(damagetype, 0d);
            }
        }

        return ret;
    }

    public double reflection(DamageType damageType) {
        if(reflections.containsKey(damageType)) {
            return reflections.get(damageType);
        }
        return 0d;
    }

    private void simplify() {
        Iterator<Map.Entry<DamageType, Double>> iter = reflections.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<DamageType, Double> entry = iter.next();
            if(entry.getValue() <= 0) {
                iter.remove();
            }
        }
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof ReflectDamageModifier reflectmod) {
            for(DamageType damagetype : reflectmod.damagetype().children()) {
                reflections.put(damagetype, reflection(damagetype) + reflectmod.value() / 100d);
            }
        }

        simplify();
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof ReflectDamageModifier reflectmod) {
            for(DamageType damagetype : reflectmod.damagetype().children()) {
                reflections.put(damagetype, reflection(damagetype) - reflectmod.value() / 100d);
            }
        }

        simplify();
    }
}
