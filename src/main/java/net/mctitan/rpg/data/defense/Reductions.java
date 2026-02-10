package net.mctitan.rpg.data.defense;

import net.mctitan.rpg.enums.DamageType;

import java.util.HashMap;
import java.util.Map;

public class Reductions {
    private transient Map<DamageType, Reduction> reductions = new HashMap<>();

    public Reductions() {
        for(DamageType type : DamageType.values()) {
            if(type.haschildren()) { continue; }
            reductions.put(type, new Reduction());
        }
    }

    public Reduction reduction(DamageType type) { return reductions.get(type); }
}
