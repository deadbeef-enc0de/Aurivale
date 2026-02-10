package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.ManaType;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.ManaModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class ManaTemplate extends ModifierTemplate {
    private ManaType manatype;
    private Operator operator;
    private double precision;
    private double flat;
    private int steps;

    public ManaTemplate(int rank, boolean base, boolean monster, String id, ManaType manatype, Operator operator,
                        double precision, double flat, int steps) {
        super(rank, base, monster, id);

        this.manatype = manatype;
        this.operator = operator;
        this.precision = precision;
        this.flat = flat;
        this.steps = steps;
    }

    public ManaTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.MANA_TYPE_KEY, PersistentDataType.STRING)) {
            String manatypestr = pdc.get(Key.MANA_TYPE_KEY, PersistentDataType.STRING);
            manatype = ManaType.valueOf(manatypestr);
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

    public ManaTemplate(ConfigurationSection section) {
        super(section);

        this.manatype = ManaType.valueOf(section.getString("manatype"));
        this.operator = Operator.valueOf(section.getString("operator"));
        this.precision = section.getDouble("precision");
        this.flat = section.getDouble("flat");
        this.steps = section.getInt("steps");
    }

    public ManaType manatype() { return manatype; }
    public Operator operator() { return operator; }
    public double precision() { return precision; }
    public double flat() { return flat; }
    public int steps() { return steps; }

    public double value(int step) {return Math.round(flat() + precision() * step, precision());}

    @Override
    public Modifier modifier(Random random) {
        double value = flat();
        if(steps() > 1) { value = value(random.nextInt(steps())); }
        return new ManaModifier(rank(), this, base(), monster(), manatype(), operator(), value);
    }

    @Override
    public String string() {
        double min = value(0);
        double max = value(steps() - 1);

        // Maximum Mana
        if(manatype() == ManaType.MAX_MANA) {
            if (operator() == Operator.FLAT) {
                String valuestr = String.format("%s%.00f", operator().string(min, base()), Math.abs(min));
                if (steps() > 1) {
                    valuestr = String.format("(%s - %s%.00f)", valuestr, operator().string(max, base()), Math.abs(max));
                }
                return String.format("%s Max Mana", valuestr);
            } else if (operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
                String valuestr = String.format("%.00f", Math.abs(min));
                if (steps() > 1) {
                    valuestr = String.format("(%s - %.00f)", valuestr, Math.abs(max));
                }
                return String.format("%s%% %s Max Mana", valuestr, operator().string(min, base()));
            } else if (operator() == Operator.SET) {
                String valuestr = String.format("%s%.00f", operator().string(min, base()), Math.abs(min));
                if (steps() > 1) {
                    valuestr = String.format("(%s - %s%.00f)", valuestr, operator().string(max, base()), Math.abs(max));
                }
                return String.format("Max Mana is %s", valuestr);
            }

        // Mana Regeneration
        } else if(manatype() == ManaType.MANA_REGEN) {
            if(operator() == Operator.FLAT) {
                String valuestr = String.format("%s%.01f", operator().string(min, base()), Math.abs(min));
                if(steps() > 1) { valuestr = String.format("(%s - %s%.01f)", valuestr, operator().string(max, base()), Math.abs(max)); }
                return String.format("%s Mana Regen", valuestr);
            } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
                String valuestr = String.format("%.00f", Math.abs(min));
                if(steps() > 1) { valuestr = String.format("(%s - %.00f)", valuestr, Math.abs(max)); }
                return String.format("%s%% %s Mana Regen", valuestr, operator().string(min, base()));
            } else if(operator() == Operator.SET) {
                String valuestr = String.format("%s%.00f", operator().string(min, base()), Math.abs(min));
                if(steps() > 1) { valuestr = String.format("(%s - %s%.00f)", valuestr, operator().string(max, base()), Math.abs(max)); }
                return String.format("Mana Regen is %s", valuestr);
            }
        }

        if(steps() > 1) {
            return String.format("Undefined Mana %s %s (%.03f - %.03f)", manatype().name(), operator().name(), min, max);
        } else {
            return String.format("Undefined Mana %s %s %.03f", manatype().name(), operator().name(), min);
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.MANA_TYPE_KEY, PersistentDataType.STRING, manatype().name());
        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, precision());
        pdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, flat());
        pdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, steps());
    }
}
