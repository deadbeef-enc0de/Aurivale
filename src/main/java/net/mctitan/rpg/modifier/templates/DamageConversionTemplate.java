package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.Conversion;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.DamageConversionModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class DamageConversionTemplate extends ModifierTemplate {
    private Conversion conversion;
    private DamageType fromtype;
    private DamageType totype;
    private double precision;
    private double flat;
    private int steps;

    public DamageConversionTemplate(int rank, boolean base, boolean monster, String id,
                                    Conversion conversion, DamageType fromtype, DamageType totype,
                                    double precision, double flat, int steps) {
        super(rank, base, monster, id);

        this.conversion = conversion;
        this.fromtype = fromtype;
        this.totype = totype;
        this.precision = precision;
        this.flat = flat;
        this.steps = steps;
    }

    public DamageConversionTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.CONVERSION_KEY, PersistentDataType.STRING)) {
            String conversionstr = pdc.get(Key.CONVERSION_KEY, PersistentDataType.STRING);
            conversion = Conversion.valueOf(conversionstr);
        }

        if(pdc.has(Key.FROM_TYPE_KEY, PersistentDataType.STRING)) {
            String damagetypestr = pdc.get(Key.FROM_TYPE_KEY, PersistentDataType.STRING);
            fromtype = DamageType.valueOf(damagetypestr);
        }

        if(pdc.has(Key.TO_TYPE_KEY, PersistentDataType.STRING)) {
            String damagetypestr = pdc.get(Key.TO_TYPE_KEY, PersistentDataType.STRING);
            totype = DamageType.valueOf(damagetypestr);
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

    public DamageConversionTemplate(ConfigurationSection section) {
        super(section);

        conversion = Conversion.valueOf(section.getString("conversion"));
        fromtype = DamageType.valueOf(section.getString("fromtype"));
        totype = DamageType.valueOf(section.getString("totype"));
        precision = section.getDouble("precision");
        flat = section.getDouble("flat");
        steps = section.getInt("steps");
    }

    public Conversion conversion() { return conversion; }
    public DamageType fromtype() { return fromtype; }
    public DamageType totype() { return totype; }
    public double precision() { return precision; }
    public double flat() { return flat; }
    public int steps() { return steps; }

    public double value(int step) { return Math.round(flat() + precision() * step, precision()); }

    @Override
    public Modifier modifier(Random random) {
        double value = flat();
        if(steps() > 1) { value = value(random.nextInt(steps())); }

        return new DamageConversionModifier(rank(), this, base(), monster(), conversion(), fromtype(), totype(), value);
    }

    @Override
    public String string() {
        String format = "%s%% %s %s %s";
        if(monster()) { format = "%s%% Monster %s %s %s"; }
        String fromstr = fromtype().longstring();
        String tostr = totype().string();

        double min = value(0);
        double max = value(steps() - 1);
        String valuestr = String.format("%.00f", min);
        if(steps() > 1) { valuestr = String.format("(%s - %.00f)", valuestr, max); }

        return String.format(format,
                valuestr,
                fromstr,
                conversion().action(),
                tostr
        );
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.CONVERSION_KEY, PersistentDataType.STRING, conversion().name());
        pdc.set(Key.FROM_TYPE_KEY, PersistentDataType.STRING, fromtype().name());
        pdc.set(Key.TO_TYPE_KEY, PersistentDataType.STRING, totype().name());
        pdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, precision());
        pdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, flat());
        pdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, steps());
    }
}
