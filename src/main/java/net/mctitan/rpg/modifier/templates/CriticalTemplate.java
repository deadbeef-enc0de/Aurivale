package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.CriticalType;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.CriticalModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class CriticalTemplate extends ModifierTemplate {
    private CriticalType criticaltype;
    private Operator operator;
    private double precision;
    private double flat;
    private int steps;

    public CriticalTemplate(int rank, boolean base, boolean monster, String id, CriticalType criticaltype, Operator operator,
                            double precision, double flat, int steps) {
        super(rank, base, monster, id);

        this.criticaltype = criticaltype;
        this.operator = operator;
        this.precision = precision;
        this.flat = flat;
        this.steps = steps;
    }

    public CriticalTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.CRITICAL_TYPE_KEY, PersistentDataType.STRING)) {
            String criticaltypestr = pdc.get(Key.CRITICAL_TYPE_KEY, PersistentDataType.STRING);
            criticaltype = CriticalType.valueOf(criticaltypestr);
        }

        if(pdc.has(Key.OPERATOR_KEY, PersistentDataType.TAG_CONTAINER)) {
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

    public CriticalTemplate(ConfigurationSection section) {
        super(section);

        this.criticaltype = CriticalType.valueOf(section.getString("criticaltype"));
        this.operator = Operator.valueOf(section.getString("operator"));
        this.precision = section.getDouble("precision");
        this.flat = section.getDouble("flat");
        this.steps = section.getInt("steps");
    }

    public CriticalType criticaltype() { return criticaltype; }
    public Operator operator() { return operator; }
    public double precision() { return precision; }
    public double flat() { return flat; }
    public int steps() { return steps; }

    public double value(int step) {return Math.round(flat() + precision() * step, precision());}

    @Override
    public Modifier modifier(Random random) {
        double value = flat();
        if (steps() > 1) { value = value(random.nextInt(steps())); }

        return new CriticalModifier(rank(), this, base(), monster(), criticaltype(), operator(), value);
    }

    @Override
    public String string() {
        double min = value(0);
        double max = value(steps() - 1);
        String criticaltypestr = criticaltype().string();

        if(operator() == Operator.FLAT) {
            if(criticaltype() == CriticalType.CRITICAL_CHANCE) {
                String valuestr = String.format("%s%.02f", operator().string(min, base()), Math.abs(min));
                if (steps() > 1) {
                    valuestr = String.format("(%s - %s%.02f)", valuestr, operator().string(max, base()), Math.abs(max));
                }
                return String.format("%s%% %s", valuestr, criticaltypestr);
            } else if(criticaltype() == CriticalType.CRITICAL_DAMAGE) {
                String valuestr = String.format("%s%.00f", operator().string(min, base()), Math.abs(min));
                if (steps() > 1) {
                    valuestr = String.format("(%s - %s%.00f)", valuestr, operator().string(max, base()), Math.abs(max));
                }
                return String.format("%s%% %s", valuestr, criticaltypestr);
            }
        } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
            String valuestr = String.format("%.00f", Math.abs(min));
            if(steps() > 1) { valuestr = String.format("(%s - %.00f)", valuestr, Math.abs(max)); }
            return String.format("%s%% %s %s", valuestr, operator().string(min, base()), criticaltypestr);
        } else if(operator() == Operator.SET) {
            String valuestr = String.format("%.02f", min);
            if(steps() > 1) { valuestr = String.format("(%s - %.02f)", valuestr, Math.abs(max)); }
            return String.format("%s is %s%%", criticaltypestr, valuestr);
        }

        if(steps() > 1) {
            return String.format("Undefined Critical %s %s (%.03f - %.03f)", criticaltype().name(), operator().name(), min, max);
        } else {
            return String.format("Undefined Critical %s %s %.03f", criticaltype().name(), operator().name(), min);
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.CRITICAL_TYPE_KEY, PersistentDataType.STRING, criticaltype().name());
        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, precision());
        pdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, flat());
        pdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, steps());
    }
}
