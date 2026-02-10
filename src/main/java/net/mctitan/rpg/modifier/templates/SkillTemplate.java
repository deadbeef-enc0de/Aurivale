package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.enums.SkillType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.SkillModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.Text;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class SkillTemplate extends ModifierTemplate {
    private SkillType skill;
    private Operator operator;
    private double precision;
    private double flat;
    private int steps;

    public SkillTemplate(int rank, boolean base, boolean monster, String id, SkillType skill, Operator operator,
                         double precision, double flat, int steps) {
        super(rank, base, monster, id);

        this.skill = skill;
        this.operator = operator;
        this.precision = precision;
        this.flat = flat;
        this.steps = steps;
    }

    public SkillTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.SKILL_KEY, PersistentDataType.STRING)) {
            String skillstr = pdc.get(Key.SKILL_KEY, PersistentDataType.STRING);
            skill = SkillType.valueOf(skillstr);
        }

        if(pdc.has(Key.OPERATOR_KEY, PersistentDataType.STRING)) {
            String operatorstr = pdc.get(Key.OPERATOR_KEY, PersistentDataType.STRING);
            operator = Operator.valueOf(operatorstr);
        }

        if(pdc.has(Key.PRECISION_KEY, PersistentDataType.DOUBLE)) {
            precision = pdc.get(Key.PRECISION_KEY, PersistentDataType.DOUBLE);
        }

        if(pdc.has(Key.FLAT_KEY, PersistentDataType.DOUBLE)) {
            flat = pdc.get(Key.FLAT_KEY, PersistentDataType.DOUBLE);
        }

        if(pdc.has(Key.STEPS_KEY, PersistentDataType.INTEGER)) {
            steps = pdc.get(Key.STEPS_KEY, PersistentDataType.INTEGER);
        }
    }

    public SkillTemplate(ConfigurationSection section) {
        super(section);

        this.skill = SkillType.valueOf(section.getString("skill"));
        this.operator = Operator.valueOf(section.getString("operator"));
        this.precision = section.getDouble("precision");
        this.flat = section.getDouble("flat");
        this.steps = section.getInt("steps");
    }

    public SkillType skill() { return skill; }
    public Operator operator() { return operator; }
    public double precision() { return precision; }
    public double flat() { return flat; }
    public int steps() { return steps; }

    public double value(int step) {return Math.round(flat() + precision() * step, precision());}

    @Override
    public Modifier modifier(Random random) {
        double value = flat();
        if(steps() > 1) { value = value(random.nextInt(steps())); }

        return new SkillModifier(rank(), this, base(), monster(), skill(), operator(), value);
    }

    @Override
    public String string() {
        String skillstr = Text.enumtoprint(skill().toString());
        double min = value(0);
        double max = value(steps() - 1);

        if(operator() == Operator.FLAT) {
            String valuestr = String.format("%s%.00f", operator().string(min, base()), Math.abs(min));
            if(steps() > 1) { valuestr = String.format("(%s - %s%.00f)", valuestr, operator().string(max, base()), Math.abs(max)); }
            return String.format("%s to %s", valuestr, skillstr);
        } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
            String valuestr = String.format("%.00f", Math.abs(min));
            if(steps() > 1) { valuestr = String.format("(%s - %.00f", valuestr, Math.abs(max)); }
            return String.format("%s%% %s %s", valuestr, operator().string(min, base()), skillstr);
        } else if(operator() == Operator.SET) {
            String valuestr = String.format("%s%.00f", operator().string(min, base()), Math.abs(min));
            if(steps() > 1) { valuestr = String.format("(%s - %s%.00f)", valuestr, operator().string(max, base()), Math.abs(max)); }
            return String.format("%s is %s", skillstr, valuestr);
        }

        if(steps() > 1) {
            return String.format("Undefined %s %s (%.03f - %.03f)", skillstr, operator().name(), min, max);
        } else {
            return String.format("Undefined %s %s %.03f", skillstr, operator().name(), min);
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.SKILL_KEY, PersistentDataType.STRING, skill().name());
        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, precision());
        pdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, flat());
        pdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, steps());
    }
}
