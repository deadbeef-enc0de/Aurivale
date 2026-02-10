package net.mctitan.rpg.data.action.attack;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.action.Action;
import net.mctitan.rpg.data.damage.Damage;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.enums.ActionName;
import net.mctitan.rpg.enums.ActionType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.*;

import java.util.List;

public class AttackAction extends Action {
    private transient AttackTime attacktime = new AttackTime();
    private transient Cancellation cancellation = new Cancellation();
    private transient Critical critical = new Critical();
    private transient Damage damage = new Damage();
    private transient Inflictions inflictions = new Inflictions();
    private transient Leech leech = new Leech();

    public AttackAction(ActionName name) { super(name, ActionType.ATTACK); }

    public AttackTime attacktime() { return attacktime; }
    public Cancellation cancellation() { return cancellation; }
    public Critical critical() { return critical; }
    public Damage damage() { return damage; }
    public Inflictions inflictions() { return inflictions; }
    public Leech leech() { return leech; }

    public void apply(Modifier modifier) {
        if(modifier instanceof AttributeModifier) {
            attacktime.apply(modifier);
        } else if(modifier instanceof CriticalModifier) {
            critical.apply(modifier);
        } else if(modifier instanceof DamageRangeModifier ||
                modifier instanceof LuckModifier ||
                modifier instanceof DamageModifier ||
                modifier instanceof DamageConversionModifier) {
            damage.apply(modifier);
        } else if(modifier instanceof StatusModifier) {
            inflictions.apply(modifier);
        } else if(modifier instanceof LeechModifier) {
            leech.apply(modifier);
        } else if(modifier instanceof DamageCancelModifier) {
            cancellation.apply(modifier);
        }
    }

    public void unapply(Modifier modifier) {
        if(modifier instanceof AttributeModifier) {
            attacktime.unapply(modifier);
        } else if(modifier instanceof CriticalModifier) {
            critical.unapply(modifier);
        } else if(modifier instanceof DamageRangeModifier ||
                modifier instanceof LuckModifier ||
                modifier instanceof DamageModifier ||
                modifier instanceof DamageConversionModifier) {
            damage.unapply(modifier);
        } else if(modifier instanceof StatusModifier) {
            inflictions.unapply(modifier);
        } else if(modifier instanceof LeechModifier) {
            leech.unapply(modifier);
        } else if(modifier instanceof DamageCancelModifier) {
            cancellation.unapply(modifier);
        }
    }

    public void apply(StatusEffect effect) {
        switch(effect.effecttype()) {
            case HASTE,MINING_FATIGUE -> attacktime.add(effect);
            case STRENGTH,WEAKNESS -> damage.add(effect);
        }
    }

    public void unapply(StatusEffect effect) {
        switch(effect.effecttype()) {
            case HASTE,MINING_FATIGUE -> attacktime.remove(effect);
            case STRENGTH,WEAKNESS -> damage.remove(effect);
        }
    }

    public AttackInstance instance(Entity entity, List<Modifier> modifiers) {
        for(Modifier modifier : modifiers) { modifier.apply(entity); }
        AttackInstance ret = instance(entity);
        for(Modifier modifier : modifiers) { modifier.unapply(entity); }

        return ret;
    }

    public AttackInstance instance(Entity entity) {
        return new AttackInstance(this, entity);
    }
}
