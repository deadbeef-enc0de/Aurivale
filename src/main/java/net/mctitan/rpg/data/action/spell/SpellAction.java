package net.mctitan.rpg.data.action.spell;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.action.Action;
import net.mctitan.rpg.data.damage.Damage;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.enums.ActionName;
import net.mctitan.rpg.enums.ActionType;
import net.mctitan.rpg.enums.SpellActivation;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.*;
import net.mctitan.rpg.spell.Spells;

import java.util.*;

public class SpellAction extends Action {
    private Map<SpellActivation, List<SpellActionData>> activations = new HashMap<>();
    private Area area = new Area();
    private Damage damage = new Damage();
    private Duration duration = new Duration();
    private Level level = new Level();

    public SpellAction(ActionName actionname) { super(actionname, ActionType.SPELL); }

    public Area area() { return area; }
    public Damage damage() { return damage.deepclone(); }
    public Duration duration() { return duration; }
    public Level level() { return level; }

    public int count(SpellActivation activation) {
        if(!activations.containsKey(activation)) {
            return 0;
        }

        return activations.get(activation).size();
    }

    public List<SpellActionData> spells(SpellActivation activation) {
        if(!activations.containsKey(activation)) {
            return List.of();
        }

        return new LinkedList<>(activations.get(activation));
    }

    public void activate(SpellActivation activation) { activate(activation, null); }
    public void activate(SpellActivation activation, Entity target) {
        List<SpellActionData> spells = spells(activation);
        if(spells.isEmpty()) { return; }

        for(SpellActionData spelldata : spells) {
            Spells.instance().activate(entity(), target, spelldata.spelltype.spell(), spelldata.level);
        }
    }

    public void apply(Modifier modifier) {
        if(modifier instanceof SpellModifier spellmod) {
            // make sure there is a mapping for the spell type
            if(!activations.containsKey(spellmod.activation())) { activations.put(spellmod.activation(), new LinkedList<>()); }

            // add a spell level pair to the mapping
            activations.get(spellmod.activation()).add(new SpellActionData(spellmod.spelltype(), spellmod.level()));
        } else if(modifier instanceof LevelModifier) {
            level.apply(modifier);
        } else if(modifier instanceof AreaModifier) {
            area.apply(modifier);
        } else if(modifier instanceof DurationModifier) {
            duration.apply(modifier);
        } else if(modifier instanceof DamageModifier) {
            damage.apply(modifier);
        } else if(modifier instanceof DamageConversionModifier) {
            damage.apply(modifier);
        }
    }

    public void unapply(Modifier modifier) {
        if(modifier instanceof SpellModifier spellmod) {
            // make sure the activation exists, if not do nothing
            if(!activations.containsKey(spellmod.activation())) {
                return;
            }

            // remove a matching spell level pair
            Iterator<SpellActionData> iter =  activations.get(spellmod.activation()).iterator();
            while(iter.hasNext()) {
                SpellActionData pair = iter.next();
                if(pair.spelltype == spellmod.spelltype() && pair.level == spellmod.level()) {
                    iter.remove();
                    break;
                }
            }

            // if there are no mappings remove the activation type
            if(activations.get(spellmod.activation()).isEmpty()) { activations.remove(spellmod.activation()); }
        } else if(modifier instanceof LevelModifier) {
            level.unapply(modifier);
        } else if(modifier instanceof AreaModifier) {
            area.unapply(modifier);
        } else if(modifier instanceof DurationModifier) {
            duration.unapply(modifier);
        } else if(modifier instanceof DamageModifier) {
            damage.unapply(modifier);
        } else if(modifier instanceof DamageConversionModifier) {
            damage.unapply(modifier);
        }
    }

    public void apply(StatusEffect effect) {}
    public void unapply(StatusEffect effect) {}
}
