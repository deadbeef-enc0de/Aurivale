package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.DamageTakenModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class DamageTakenTemplate extends ModifierTemplate {
    private DamageType damagetype;
    private Operator operator;
    private double precision;
    private double flat;
    private int steps;

    public DamageTakenTemplate(int rank, boolean base, boolean monster, String id, DamageType damagetype, Operator operator,
                               double precision, double flat, int steps) {
        super(rank, base, monster, id);

        this.damagetype = damagetype;
        this.operator = operator;
        this.precision = precision;
        this.flat = flat;
        this.steps = steps;
    }

    public DamageTakenTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING)) {
            String damagetypestr = pdc.get(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING);
            damagetype = DamageType.valueOf(damagetypestr);
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

    public DamageTakenTemplate(ConfigurationSection section) {
        super(section);

        this.damagetype = DamageType.valueOf(section.getString("damagetype"));
        this.operator = Operator.valueOf(section.getString("operator"));
        this.precision = section.getDouble("precision");
        this.flat = section.getDouble("flat");
        this.steps = section.getInt("steps");
    }

    public DamageType damagetype() { return damagetype; }
    public Operator operator() { return operator; }
    public double precision() { return precision; }
    public double flat() { return flat; }
    public int steps() { return steps; }

    public double value(int step) {return Math.round(flat() + precision() * step, precision());}

    @Override
    public Modifier modifier(Random random) {
        double value = flat();
        if (steps() > 1) { value = value(random.nextInt(steps())); }

        return new DamageTakenModifier(rank(), this, base(), monster(), damagetype(), operator(), value);
    }

    @Override
    public String string() {
        double min = value(0);
        double max = value(steps() - 1);

        if(operator() == Operator.FLAT) {
            String valuestr = String.format("%s%.00f", operator().string(min, base()), Math.abs(min));
            if(steps() > 1) { valuestr = String.format("(%s - %s%.00f)", valuestr, operator().string(max, base()), Math.abs(max)); }
            return String.format("%s %s Damage Taken", valuestr, damagetype().string());

        } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
            String valuestr = String.format("%.00f", Math.abs(min));
            if(steps() > 1) { valuestr = String.format("(%s - %.00f)", valuestr, Math.abs(max)); }
            return String.format("%s%% %s %s Damage Taken", valuestr, operator().string(min, base()), damagetype().string());
        }

        if(steps() > 1) {
            return String.format("Undefined Damage Taken %s %s (%.03f - %.03f)", damagetype().name(), operator().name(), min, max);
        } else {
            return String.format("Undefined Damage Taken %s %s %.03f", damagetype().name(), operator().name(), min);
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING, damagetype().name());
        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, precision());
        pdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, flat());
        pdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, steps());
    }
}
