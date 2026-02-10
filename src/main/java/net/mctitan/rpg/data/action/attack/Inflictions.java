package net.mctitan.rpg.data.action.attack;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.StatusModifier;

import java.util.HashSet;
import java.util.Set;

public class Inflictions extends Modable {
    private HashSet<StatusEffect> inflictions = new HashSet<>();

    public Inflictions() {}

    private Inflictions(Inflictions other) {
        inflictions.addAll(other.inflictions);
    }

    public Inflictions clone() { return new Inflictions(this); }

    public Set<StatusEffect> effects() { return new HashSet<>(inflictions); }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof StatusModifier statusmod) {
            inflictions.add(statusmod.statuseffect());
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof StatusModifier statusmod) {
            inflictions.remove(statusmod.statuseffect());
        }
    }
}
