package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.Attribute;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.AttributeModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class AttributeTemplate extends ModifierTemplate {
    private Attribute attribute;
    private Operator operator;
    private double precision;
    private double flat;
    private int steps;

    public AttributeTemplate(int rank, boolean base, boolean monster, String id, Attribute attribute, Operator operator,
                             double precision, double flat, int steps) {
        super(rank, base, monster, id);

        this.attribute = attribute;
        this.operator = operator;
        this.precision = precision;
        this.flat = flat;
        this.steps = steps;
    }
    public AttributeTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.ATTRIBUTE_KEY, PersistentDataType.STRING)) {
            String attributestr = pdc.get(Key.ATTRIBUTE_KEY, PersistentDataType.STRING);
            attribute = Attribute.valueOf(attributestr);
        }

        if(pdc.has(Key.OPERATOR_KEY, PersistentDataType.STRING)) {
            String operationstr = pdc.get(Key.OPERATOR_KEY, PersistentDataType.STRING);
            operator = Operator.valueOf(operationstr);
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

    public AttributeTemplate(ConfigurationSection section) {
        super(section);

        this.attribute = Attribute.valueOf(section.getString("attribute"));
        this.operator = Operator.valueOf(section.getString("operator"));
        this.precision = section.getDouble("precision");
        this.flat = section.getDouble("flat");
        this.steps = section.getInt("steps");
    }

    public Attribute attribute() { return attribute; }
    public Operator operator() { return operator; }
    public double precision() { return precision; }
    public double flat() { return flat; }
    public int steps() { return steps; }

    public double value(int step) {return Math.round(flat() + precision() * step, precision());}

    @Override
    public Modifier modifier(Random random) {
        double value = flat();
        if(steps() > 1) { value = value(random.nextInt(steps())); }

        return new AttributeModifier(rank(), this, base(), monster(), attribute(), operator(), value);
    }

    @Override
    public String string() {
        double min = value(0);
        double max = value(steps() - 1);

        double localmin = (attribute() == Attribute.ATTACK_SPEED ? 1 / min : min);
        double localmax = (attribute() == Attribute.ATTACK_SPEED ? 1 / max : max);

        String attributename = attribute().string();
        if(monster()) { attributename = String.format("Monster %s", attributename); }

        if(operator() == Operator.FLAT) {
            String format = String.format("%%s%s", attribute().flatformat());
            String valuestr = String.format(format, operator().string(localmin, base()), Math.abs(localmin));
            if(steps() > 1) {
                format = String.format("(%%s - %s)", format);
                valuestr = String.format(format, valuestr, operator().string(localmax, base()), localmax);
            }
            return String.format("%s to %s", valuestr, attributename);
        } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
            String valuestr = String.format(attribute().percentformat(), Math.abs(min));
            if(steps() > 1) {
                String format = String.format("(%%s - %s)", attribute().percentformat());
                valuestr = String.format(format, valuestr, Math.abs(max));
            }
            return String.format("%s%% %s %s", valuestr, operator().string(localmin, base()), attributename);
        } else if(operator() == Operator.SET) {
            String format = String.format("%%s%s", attribute().flatformat());
            String valuestr = String.format(format, operator().string(localmin, base()), Math.abs(localmin));
            if(steps() > 1) {
                format = String.format("(%%s - %s)", format);
                valuestr = String.format(format, valuestr, operator().string(localmax, base()), localmax);
            }
            return String.format("%s is %s", attributename, valuestr);
        }

        if(steps() > 1) {
            return String.format("Undefined %s %s (%.03f - %.03f)", attributename, operator().name(), localmin, localmax);
        } else {
            return String.format("Undefined %s %s %.03f", attributename, operator().name(), localmin);
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.ATTRIBUTE_KEY, PersistentDataType.STRING, attribute().name());
        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, precision());
        pdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, flat());
    }
}
