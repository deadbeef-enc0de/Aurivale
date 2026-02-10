package net.mctitan.rpg.data.damage;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.DamageConversionModifier;
import net.mctitan.rpg.modifier.types.DamageModifier;
import net.mctitan.rpg.modifier.types.DamageRangeModifier;
import net.mctitan.rpg.modifier.types.LuckModifier;
import net.mctitan.rpg.util.Math;

import java.util.HashMap;
import java.util.Map;

public class Damage extends Modable {
    private DamageConversion conversions = new DamageConversion();
    private HashMap<DamageType, DamagePart> damages = new HashMap<>();
    private int attackluck = 0;
    private double doublechance = 0;

    public Damage() {}

    public Damage(Damage other) {
        for(DamageType damagetype : other.damages.keySet()) {
            damages.put(damagetype, other.damages.get(damagetype).clone());
        }
        conversions = other.conversions.clone();
        attackluck = other.attackluck;
        doublechance = other.doublechance;
    }

    public Damage clone() {
        return new Damage(this);
    }
    public Damage deepclone() {
        Damage damage = new Damage();
        for(Modifier modifier : modifiers()) {
            damage.apply(modifier);
        }

        return damage;
    }

    public DamageConversion conversions() { return conversions; }
    public Map<DamageType, DamagePart> damages() { return new HashMap<>(damages); }
    public boolean attackluck() { return attackluck > 0; }

    public DamageRoll roll() {
        // Roll Damage
        DamageRoll roll = singleroll();
        if(attackluck()) {
            DamageRoll second = singleroll();
            roll = second.damage() > roll.damage() ? second : roll;
        }

        // Roll for double damage
        if(Math.nextDouble() < doublechance) {
            roll.multiply(2);
        }

        // Apply damage conversions
        conversions.apply(roll);

        return roll;
    }

    public DamageRoll singleroll() {
        DamageRoll ret = new DamageRoll();
        for(Map.Entry<DamageType, DamagePart> entry : damages.entrySet()) {
            ret.add(entry.getKey(), entry.getValue());
        }

        return ret;
    }

    public void addflat(DamageType damagetype, double minimum, double maximum) {
        if(!damages.containsKey(damagetype)) {
            damages.put(damagetype, new DamagePart());
        }
        damages.get(damagetype).addflat(minimum, maximum);
    }

    @Override
    protected void add(Modifier modifier) {
        // Damage Range Modifier
        if(modifier instanceof DamageRangeModifier damagemod) {
            for(DamageType damagetype : damagemod.damageType().children()) {
                if(!damages.containsKey(damagetype)) {
                    damages.put(damagetype, new DamagePart());
                }
                damages.get(damagetype).apply(modifier);
            }
        }

        // Damage Modifier
        else if(modifier instanceof DamageModifier damagemod) {
            for(DamageType damagetype : damagemod.damagetype().children()) {
                if(!damages.containsKey(damagetype)) {
                    damages.put(damagetype, new DamagePart());
                }
                damages.get(damagetype).apply(modifier);
            }
        }

        // Damage Conversion
        else if(modifier instanceof DamageConversionModifier) {
            conversions.apply(modifier);
        }

        // attack luck stuff
        else if(modifier instanceof LuckModifier luckmod) {
            switch(luckmod.lucktype()) {
                case LUCKY_DAMAGE -> { attackluck += 1; }
                case DOUBLE_DAMAGE -> { doublechance += luckmod.value() / 100d; }
            }
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        // Damage Range Modifier
        if(modifier instanceof DamageRangeModifier damagemod) {
            for(DamageType damagetype : damagemod.damageType().children()) {
                damages.get(damagetype).unapply(modifier);
            }
        }

        // Damage Modifier
        else if(modifier instanceof DamageModifier damagemod) {
            for(DamageType damagetype : damagemod.damagetype().children()) {
                damages.get(damagetype).unapply(modifier);
            }
        }

        // Damage Conversion
        else if(modifier instanceof DamageConversionModifier) {
            conversions.unapply(modifier);
        }

        // attack luck stuff
        else if(modifier instanceof LuckModifier luckmod) {
            switch(luckmod.lucktype()) {
                case LUCKY_DAMAGE -> attackluck -= 1;
                case DOUBLE_DAMAGE -> doublechance -= luckmod.value() / 100d;
            }
        }
    }

    public void add(StatusEffect effect) {
        for(DamageType damagetype : DamageType.ALL.children()) {
            if(!damages.containsKey(damagetype)) {
                damages.put(damagetype, new DamagePart());
            }
            damages.get(damagetype).add(effect);
        }
    }

    public void remove(StatusEffect effect) {
        for(DamageType damagetype : DamageType.ALL.children()) {
            damages.get(damagetype).remove(effect);
        }
    }
}
