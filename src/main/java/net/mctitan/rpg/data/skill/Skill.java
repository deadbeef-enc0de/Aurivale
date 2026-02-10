package net.mctitan.rpg.data.skill;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.SkillModifier;
import net.mctitan.rpg.util.Math;

import java.util.HashMap;
import java.util.Map;

public class Skill extends Modable {
    private static final int MAXIMUM_ROLL = 20;

    // values used to calculate skill value
    private transient double base = 0;
    private transient double scaler = 1;
    private transient Map<Modifier, Double> multis = new HashMap<>();

    // skill value
    private transient int value;

    public int value() { return value; }

    /**
     * @return value + 0 -> MAXIMUM_ROLL
     */
    public int roll() { return value() + Math.nextInt(MAXIMUM_ROLL + 1); }

    private void calculate() {
        double value = base * scaler;
        for (Double multi : multis.values()) {
            value *= (1 + multi);
        }
        this.value = (int)value;
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof SkillModifier skillmod) {
            switch(skillmod.operator()) {
                case FLAT -> base += skillmod.value();
                case SCALER -> scaler += skillmod.value() / 100d;
                case MULTIPLIER -> multis.put(skillmod, skillmod.value() / 100d);
            }
            calculate();
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof SkillModifier skillmod) {
            switch(skillmod.operator()) {
                case FLAT -> base -= skillmod.value();
                case SCALER -> scaler -= skillmod.value() / 100d;
                case MULTIPLIER -> multis.remove(skillmod);
            }
            calculate();
        }
    }
}
