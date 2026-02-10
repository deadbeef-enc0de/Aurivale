package net.mctitan.rpg.data.defense;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.DamageConversionModifier;

import java.util.HashMap;

public class DefenseConversion extends Modable {
    private HashMap<DamageType, HashMap<DamageType, Double>> conversions = new HashMap<>();

    public void apply(DamageRoll roll) {
        DamageRoll converted = new DamageRoll();
        for(DamageType from : roll.damagetypes()) {
            HashMap<DamageType, Double> conversion = conversions.get(from);
            if(conversion != null) {
                double totalconvert = 0;
                for(DamageType to : conversion.keySet()) { totalconvert += conversion.get(to); }
                totalconvert = Math.max(totalconvert, 1);

                for(DamageType to : conversion.keySet()) {
                    double damage = roll.damage(from) * conversion.get(to) / totalconvert;
                    converted.add(from, -damage);
                    converted.add(to, damage);
                }
            }
        }

        roll.add(converted);
    }

    private void change(DamageType from, DamageType to, double change) {
        if(!conversions.containsKey(from)) { conversions.put(from, new HashMap<>()); }
        if(!conversions.get(from).containsKey(to)) { conversions.get(from).put(to, 0d); }
        conversions.get(from).put(to, change + conversions.get(from).get(to));
        if(conversions.get(from).get(to) <= 0) { conversions.get(from).remove(to); }
        if(conversions.get(from).isEmpty()) { conversions.remove(from); }
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof DamageConversionModifier convertmod) {
            for(DamageType from : convertmod.fromtype().children()) {
                for(DamageType to : convertmod.totype().children()) {
                    switch(convertmod.conversion()) {
                        case TAKEN -> change(from, to, convertmod.value() / 100d);
                    }
                }
            }
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof DamageConversionModifier convertmod) {
            for(DamageType from : convertmod.fromtype().children()) {
                for(DamageType to : convertmod.totype().children()) {
                    switch(convertmod.conversion()) {
                        case TAKEN -> change(from, to, -convertmod.value() / 100d);
                    }
                }
            }
        }
    }
}
