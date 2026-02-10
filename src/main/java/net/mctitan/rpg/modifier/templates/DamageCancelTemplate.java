package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.CancelType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.DamageCancelModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class DamageCancelTemplate extends ModifierTemplate {
    private CancelType canceltype;
    private double precision;
    private double flat;
    private int steps;

    public DamageCancelTemplate(int rank, boolean base, boolean monster, String id, CancelType canceltype,
                                double precision, double flat, int steps) {
        super(rank, base, monster, id);

        this.canceltype = canceltype;
        this.precision = precision;
        this.flat = flat;
        this.steps = steps;
    }

    public DamageCancelTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.CANCEL_TYPE_KEY, PersistentDataType.STRING)) {
            String canceltypestr = pdc.get(Key.CANCEL_TYPE_KEY, PersistentDataType.STRING);
            this.canceltype = CancelType.valueOf(canceltypestr);
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

    public DamageCancelTemplate(ConfigurationSection section) {
        super(section);

        this.canceltype = CancelType.valueOf(section.getString("canceltype"));
        this.precision = section.getDouble("precision");
        this.flat = section.getDouble("flat");
        this.steps = section.getInt("steps");
    }

    public CancelType canceltype() { return canceltype; }
    public double precision() { return precision; }
    public double flat() { return flat; }
    public int steps() { return steps; }

    public double value(int step) {return Math.round(flat() + precision() * step, precision());}

    @Override
    public Modifier modifier(Random random) {
        double value = flat();
        if (steps() > 1) { value = value(random.nextInt(steps())); }

        return new DamageCancelModifier(rank(), this, base(), monster(), canceltype(), value);
    }

    @Override
    public String string() {
        double min = value(0);
        double max = value(steps() - 1);
        String valuestr = String.format("%.00f", min);
        if(steps() > 1) { valuestr = String.format("(%s - %.00f)", valuestr, max); }
        String typestr = canceltype().string();

        if(min >= 100) {
            return String.format("Deal no %s Damage", typestr);
        } else {
            return String.format("Deal %s%% Less %s Damage", valuestr, typestr);
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.CANCEL_TYPE_KEY, PersistentDataType.STRING, canceltype().name());
        pdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, precision());
        pdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, flat());
        pdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, steps());
    }
}
