package net.mctitan.rpg.data.damage;

import net.mctitan.data.BasicData;
import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.DamageConversionModifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DamageConversion extends Modable {
    private HashMap<DamageType, DamageMap> conversions = new HashMap<>();
    private HashMap<DamageType, DamageMap> extras = new HashMap<>();

    public DamageConversion() {}

    public DamageConversion(DamageConversion other) {
        for(DamageType from : other.conversions.keySet()) {
            conversions.put(from, other.conversions.get(from).clone());
        }

        for(DamageType from : other.extras.keySet()) {
            extras.put(from, other.extras.get(from).clone());
        }
    }

    public DamageConversion clone() { return new DamageConversion(this); }

    public Map<DamageType, DamagePart> apply(Map<DamageType, DamagePart> input) {
        Map<DamageType, DamagePart> ret =  new HashMap<>();

        // go through all incput damage types
        for(DamageType from : input.keySet()) {
            DamagePart part = input.get(from).clone();
            if (ret.containsKey(from)) {
                ret.get(from).add(part);
            } else {
                ret.put(from, part.clone());
            }

            // handle conversion
            DamageMap conversion = conversions.get(from);
            if(conversion != null) {
                double totalconvert = 0;
                for (DamageType to : conversion.types()) { totalconvert += conversion.get(to); }
                totalconvert = Math.max(totalconvert, 1);

                for(DamageType to : conversion.types()) {
                    DamagePart convert = new DamagePart(
                            part.minimum() * conversion.get(to) / totalconvert,
                            part.maximum() * conversion.get(to) / totalconvert
                    );

                    // remove from damage
                    if(ret.containsKey(from)) {
                        ret.get(from).subtract(convert);
                    } else {
                        ret.put(from, new DamagePart(-convert.minimum(), -convert.maximum()));
                    }

                    // remove to damage
                    if(ret.containsKey(to)) {
                        ret.get(to).add(convert);
                    } else {
                        ret.put(to, convert);
                    }
                }
            }

            // handle extra
            DamageMap extra = extras.get(from);
            if(extra != null) {
                for(DamageType to : extra.types()) {
                    DamagePart add = new DamagePart(part.minimum() * extra.get(to), part.maximum() * extra.get(to));
                    if(ret.containsKey(to)) {
                        ret.get(to).add(add);
                    } else {
                        ret.put(to, add);
                    }
                }
            }
        }

        return ret;
    }

    public void apply(DamageRoll roll) {
        // apply to incoming damage roll
        DamageRoll converted = new DamageRoll();
        DamageRoll added = new DamageRoll();
        for(DamageType from : roll.damagetypes()) {
            // handle conversion
            DamageMap conversion = conversions.get(from);
            if(conversion != null) {
                double totalconvert = 0;
                for (DamageType to : conversion.types()) { totalconvert += conversion.get(to); }
                totalconvert = Math.max(totalconvert, 1);

                for (DamageType to : conversion.types()) {
                    double damage = roll.damage(from) * conversion.get(to) / totalconvert;
                    converted.add(from, -damage);
                    converted.add(to, damage);
                }
            }

            // handle extra
            DamageMap extra = extras.get(from);
            if(extra != null) {
                for(DamageType to : extra.types()) {
                    double damage = roll.damage(from) * extra.get(to);
                    added.add(to, damage);
                }
            }
        }

        // apply conversion and extra
        roll.add(converted);
        roll.add(added);
    }

    private void changeconversion(DamageType from, DamageType to, double change) { change(from, to, conversions, change); }
    private void changeextra(DamageType from, DamageType to, double change) { change(from, to, extras, change); }
    private void change(DamageType from, DamageType to, Map<DamageType, DamageMap> map, double change) {
        if(!map.containsKey(from)) { map.put(from, new DamageMap()); }
        map.get(from).change(to, change);
        if(map.get(from).empty()) { map.remove(from); }
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof DamageConversionModifier convertmod) {
            for(DamageType from : convertmod.fromtype().children()) {
                for(DamageType to : convertmod.totype().children()) {
                    switch(convertmod.conversion()) {
                        case DAMAGE -> changeconversion(from, to, convertmod.value() / 100d);
                        case EXTRA_DAMAGE -> changeextra(from, to, convertmod.value() / 100d);
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
                        case DAMAGE -> changeconversion(from, to, -convertmod.value() / 100d);
                        case EXTRA_DAMAGE -> changeextra(from, to, -convertmod.value() / 100d);
                    }
                }
            }
        }
    }

    public static class DamageMap extends BasicData {
        private HashMap<DamageType, Double> map = new HashMap<>();

        public DamageMap() {}

        public DamageMap(DamageMap other) {
            map.putAll(other.map);
        }

        public DamageMap clone() { return new DamageMap(this); }

        public boolean empty() { return map.isEmpty(); }
        public Set<DamageType> types() { return new HashSet<>(map.keySet()); }
        public double get(DamageType type) { return map.getOrDefault(type, 0d); }
        public void change(DamageType type, double change) {
            if(!map.containsKey(type)) { map.put(type, 0d); }
            map.put(type, map.get(type) + change);
            if(map.get(type) <= 0) { map.remove(type); }
        }
    }
}
