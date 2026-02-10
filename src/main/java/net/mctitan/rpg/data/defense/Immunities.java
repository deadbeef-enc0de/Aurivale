package net.mctitan.rpg.data.defense;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.enums.EffectType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.StatusModifier;

import java.util.HashMap;
import java.util.Map;

public class Immunities extends Modable {
    private transient Map<EffectType,Integer> effects = new HashMap<>();

    public boolean immune(EffectType effecttype) { return effects.containsKey(effecttype) && effects.get(effecttype) > 0; }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof StatusModifier statusmod) {
            switch(statusmod.statustype()) {
                case IMMUNITY -> {
                    if(!effects.containsKey(statusmod.effecttype())) { effects.put(statusmod.effecttype(), 0); }
                    effects.put(statusmod.effecttype(), effects.get(statusmod.effecttype()) + 1);
                }
            }
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof StatusModifier statusmod) {
            switch(statusmod.statustype()) {
                case IMMUNITY -> {
                    effects.put(statusmod.effecttype(), effects.get(statusmod.effecttype()) - 1);
                    if(effects.get(statusmod.effecttype()) <= 0) {
                        effects.remove(statusmod.effecttype());
                    }
                }
            }
        }
    }
}
