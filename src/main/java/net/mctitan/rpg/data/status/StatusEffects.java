package net.mctitan.rpg.data.status;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.enums.EffectType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.StatusModifier;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.comparators.StatusEffectInstanceComparator;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.logging.Level;

public class StatusEffects extends Modable implements Logger {
    private transient Entity entity;
    private transient HashMap<StatusEffect, StatusEffectInstance> effects = new HashMap<>();
    private transient HashMap<EffectType, StatusEffectInstance> statuses = new HashMap<>();
    private transient HashMap<EffectType, TreeSet<StatusEffectInstance>> instances = new HashMap<>();
    private HashSet<StatusEffectInstance> timed = new HashSet<>();
    private HashSet<StatusEffectInstance> external = new HashSet<>();

    public StatusEffects() {
        for(EffectType type : EffectType.values()) {
            instances.put(type, new TreeSet<>(new StatusEffectInstanceComparator()));
        }
    }

    public Entity entity() { return entity; }
    public void entity(Entity entity) {
        this.entity = entity;

        // add effects to the other data structures
        HashSet<StatusEffectInstance> initialize = new HashSet<>();
        initialize.addAll(timed);
        initialize.addAll(external);
        for(StatusEffectInstance instance : initialize) {
            instance.entity(entity);
            add(instance);
        }
    }

    public int numbereffects() { return statuses.size();}

    public Set<StatusEffectInstance> effects() { return new HashSet<>(statuses.values()); }

    public void removeall() {
        log(Level.INFO, String.format("Removing effects from %s", entity.name()));

        // get all instances on the entity
        HashSet<StatusEffectInstance> cleared = new HashSet<>();
        for(EffectType type : instances.keySet()) {
            for(StatusEffectInstance instance : instances.get(type)) {
                cleared.add(instance);
            }
        }

        // remove each instance
        for(StatusEffectInstance instance : cleared) {
            remove(instance);
        }
    }

    public void finished(EffectType effecttype) {
        if(!statuses.containsKey(effecttype)) {
            return;
        }

        StatusEffectInstance instance = statuses.get(effecttype);
        remove(instance);
    }

    public void cleared(EffectType effecttype) {
        if(!statuses.containsKey(effecttype)) {
            return;
        }

        StatusEffectInstance instance = statuses.get(effecttype);
        remove(instance, false);
    }

    private void apply(StatusEffectInstance instance) {
        log(Level.INFO, String.format("%s applying %s level-%d %.02fs",
                entity.name(),
                instance.effect().effecttype().name(),
                instance.effect().level(),
                instance.remaining() / 20d
        ));

        // add potion effect ot player
        PotionEffect effect = instance.bukkit(entity);
        entity.bukkitentity().addPotionEffect(effect);

        // add instances to entity statuses
        statuses.put(instance.effect().effecttype(), instance);

        // add effect to actions
        entity.actions().apply(instance.effect());
    }

    private void unapply(StatusEffectInstance instance) {
        log(Level.INFO, String.format("%s unapplying %s level-%d %.02fs",
                entity.name(),
                instance.effect().effecttype().name(),
                instance.effect().level(),
                instance.remaining() / 20d
        ));

        // remove potion effect
        if(entity.bukkitentity() != null) {
            entity.bukkitentity().removePotionEffect(instance.effect().effecttype().potion());
        }

        // remove instance from entity statuses
        statuses.remove(instance.effect().effecttype());

        // remove effect from actions
        entity.actions().unapply(instance.effect());
    }

    private boolean running(StatusEffectInstance instance) {
        return statuses.containsKey(instance.effect().effecttype()) &&
               statuses.get(instance.effect().effecttype()) == instance;
    }

    public void addexternal(StatusEffect effect) {
        StatusEffectInstance instance = effect.instance(entity);
        if(instance.effect().permanent()) {
            external.add(instance);
        }
        add(instance);
    }

    private void add(StatusEffect effect) {
        StatusEffectInstance instance = effect.instance(entity);
        add(instance);
    }

    private void add(StatusEffectInstance instance) {
        log(Level.INFO, String.format("%s adding %s level-%d %.02fs",
                entity.name(),
                instance.effect().effecttype().name(),
                instance.effect().level(),
                instance.remaining() / 20d
        ));

        EffectType effecttype = instance.effect().effecttype();

        // add instance to collections
        effects.put(instance.effect(), instance);
        instances.get(effecttype).add(instance);
        if(!instance.effect().permanent()) {
            timed.add(instance);
        } else if(instance.effect().permanent()) {
            external.add(instance);
        }

        // get current status of this type
        StatusEffectInstance current = statuses.get(effecttype);

        // check to see if we should apply the added instance now
        if(current == null ||
                instance.effect().level() > current.effect().level() ||
                ( instance.effect().permanent() && !current.effect().permanent() ) ||
                ( instance.effect().level() == current.effect().level() && instance.remaining() > current.remaining() )
        ) {
            // remove current status if it exists
            if(current != null) {
                // we are overriding a same level but longer duration effect
                if(instance.effect().level() == current.effect().level() && !current.effect().permanent()) {
                    // fully removing the effect will apply the new one automatically
                    remove(current);

                // we are covering up a lower level effect
                } else {
                    // remove the effect
                    unapply(current);

                    // apply status
                    apply(instance);
                }

            // there was no old effect, apply the new one
            } else {
                apply(instance);
            }
        }
    }

    private void remove(StatusEffect effect) {
        StatusEffectInstance instance = effects.get(effect);
        if(instance == null) { return; }

        remove(instance);
    }

    private void remove(StatusEffectInstance instance) { remove(instance, true); }
    private void remove(StatusEffectInstance instance, boolean remove) {
        log(Level.INFO, String.format("%s removing %s level-%d %.02fs",
                entity.name(),
                instance.effect().effecttype().name(),
                instance.effect().level(),
                instance.remaining() / 20d
        ));

        EffectType effecttype = instance.effect().effecttype();

        // remove instance from various collections
        effects.remove(instance.effect());
        instances.get(effecttype).remove(instance);
        if(!instance.effect().permanent()) {
            timed.remove(instance);
        }
        external.remove(instance);

        // if the instance is the current running one, remove it
        if(running(instance)) {
            if(remove) {
                unapply(instance);

                // get the next effect to run
                while (!instances.get(effecttype).isEmpty()) {
                    StatusEffectInstance next = instances.get(effecttype).getFirst();

                    // make sure next effect should be applied, if not remove it
                    if (next.effect().permanent() || next.remaining() > 0) {
                        apply(next);
                        break;
                    } else {
                        remove(next);
                    }
                }
            } else {
                // remove instance from entity statuses
                statuses.remove(instance.effect().effecttype());

                // remove effect from actions
                entity.actions().unapply(instance.effect());

                // remove all other instances with the same type
                for(StatusEffectInstance other : instances.get(instance.effect().effecttype()).stream().toList()) {
                    remove(other);
                }
            }
        }
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof StatusModifier statusmod) {
            switch(statusmod.statustype()) {
                case PERMANENT, TEMPORARY -> add(statusmod.statuseffect());
            }
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof StatusModifier statusmod) {
            switch(statusmod.statustype()) {
                case PERMANENT, TEMPORARY -> remove(statusmod.statuseffect());
            }
        }
    }
}
