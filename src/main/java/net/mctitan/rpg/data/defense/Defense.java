package net.mctitan.rpg.data.defense;

import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.enums.DamageType;

public class Defense {
    private transient Blocking blocking = new Blocking();
    private transient DamageReflection damagereflection = new DamageReflection();
    private transient DefenseConversion conversion = new DefenseConversion();
    private transient Immunities immunities = new Immunities();
    private transient Reductions reductions = new Reductions();

    public Blocking blocking() { return blocking; }
    public DamageReflection damagereflection() { return damagereflection; }
    public DefenseConversion conversion() { return conversion; }
    public Immunities immunities() { return immunities; }
    public Reductions reductions() { return reductions; }

    public DamageRoll apply(DamageRoll roll) {
        DamageRoll ret = roll.clone();

        // apply conversions
        conversion.apply(ret);

        // apply reductions
        for(DamageType damagetype : ret.damagetypes()) {
            double damage = ret.damage(damagetype);
            Reduction reduction = reductions.reduction(damagetype);
            ret.set(damagetype, reduction.apply(damage));
        }

        return ret;
    }
}
