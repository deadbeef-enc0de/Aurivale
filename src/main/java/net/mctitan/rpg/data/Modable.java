package net.mctitan.rpg.data;

import net.mctitan.data.BasicData;
import net.mctitan.rpg.modifier.Modifier;

import java.util.HashSet;
import java.util.Set;

public abstract class Modable extends BasicData {
    private transient Set<Modifier> modifiers = new HashSet<>();

    public void apply(Modifier modifier) {
        if(modifiers.contains(modifier)) {
            return;
        }
        modifiers.add(modifier);
        add(modifier);
    }

    public void unapply(Modifier modifier) {
        if(!modifiers.contains(modifier)) {
            return;
        }
        modifiers.remove(modifier);
        remove(modifier);
    }

    public Set<Modifier> modifiers() {
        return new HashSet<>(modifiers);
    }

    protected abstract void add(Modifier modifier);
    protected abstract void remove(Modifier modifier);
}
