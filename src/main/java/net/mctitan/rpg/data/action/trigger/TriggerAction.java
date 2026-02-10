package net.mctitan.rpg.data.action.trigger;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.action.Action;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.enums.ActionName;
import net.mctitan.rpg.enums.ActionType;
import net.mctitan.rpg.enums.StatusType;
import net.mctitan.rpg.enums.TriggerType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.StatusModifier;
import net.mctitan.rpg.modifier.types.TriggerModifier;

import java.util.*;

public class TriggerAction extends Action {
    private Map<TriggerType, Set<TriggerModifier>> modifiers = new HashMap<>();

    public TriggerAction(ActionName name) { super(name, ActionType.TRIGGER); }

    public void activate(TriggerType type) {
        for(TriggerModifier modifier : modifiers(type)) {
            add(this.entity(), modifier);
        }
    }

    public void deactivate(TriggerType type) {
        for(TriggerModifier modifier : modifiers(type)) {
            remove(this.entity(), modifier);
        }
    }

    private Collection<TriggerModifier> modifiers(TriggerType type) {
        if(!modifiers.containsKey(type)) {
            return List.of();
        }
        return new HashSet<>(modifiers.get(type));
    }

    public void apply(Modifier modifier) {
        if(modifier instanceof TriggerModifier triggermod) {
            // make sure we have a mapping to use
            if(!modifiers.containsKey(triggermod.triggertype())) { modifiers.put(triggermod.triggertype(), new HashSet<>()); }

            // add the modifier to the mapping
            modifiers.get(triggermod.triggertype()).add(triggermod);
        }
    }

    public void unapply(Modifier modifier) {
        if(modifier instanceof TriggerModifier triggermod) {
            // remove the triggered modifier from the player in case it's active
            remove(entity(), triggermod);

            // remove the modifier from the mapping
            modifiers.get(triggermod.triggertype()).remove(triggermod);

            // if there are no more remove the mapping
            if(modifiers.get(triggermod.triggertype()).isEmpty()) { modifiers.remove(triggermod.triggertype()); }
        }
    }

    public void apply(StatusEffect effect) {}
    public void unapply(StatusEffect effect) {}

    public static void add(Entity entity, TriggerModifier triggermod) {
        if((triggermod.modifier() instanceof StatusModifier statusmod) &&
                statusmod.statustype() == StatusType.TEMPORARY &&
                triggermod.effect()) {
            entity.statuseffects().addexternal(statusmod.statuseffect());
        } else {
            triggermod.modifier().apply(entity);
        }
    }

    public static void remove(Entity entity, TriggerModifier triggermod) {
        if((triggermod.modifier() instanceof StatusModifier statusmod) &&
                statusmod.statustype() == StatusType.TEMPORARY &&
                triggermod.effect()) {
            // do nothing for self applied inflictions
        } else {
            triggermod.modifier().unapply(entity);
        }
    }
}
