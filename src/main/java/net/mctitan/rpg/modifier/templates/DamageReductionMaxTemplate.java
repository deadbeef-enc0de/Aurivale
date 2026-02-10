package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.DamageReductionMaxModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class DamageReductionMaxTemplate extends ModifierTemplate {
    private DamageType damagetype;
    private double precision;
    private double flat;
    private int steps;

    public DamageReductionMaxTemplate(int rank, boolean base, boolean monster, String id, DamageType damagetype,
                                      double precision, double flat, int steps) {
        super(rank, base, monster, id);

        this.damagetype = damagetype;
        this.precision = precision;
        this.flat = flat;
        this.steps = steps;
    }

    public DamageReductionMaxTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING)) {
            String damagetypestr = pdc.get(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING);
            damagetype = DamageType.valueOf(damagetypestr);
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

    public DamageReductionMaxTemplate(ConfigurationSection section) {
        super(section);

        this.damagetype = DamageType.valueOf(section.getString("damagetype"));
        this.precision = section.getDouble("precision");
        this.flat = section.getDouble("flat");
        this.steps = section.getInt("steps");
    }

    public DamageType damagetype() { return damagetype; }
    public double precision() { return precision; }
    public double flat() { return flat; }
    public int steps() { return steps; }

    public double value(int step) { return Math.round(flat() + precision() * step, precision()); }

    @Override
    public Modifier modifier(Random random) {
        double resistance = flat;
        if(steps > 1) { resistance = value(random.nextInt(steps())) ; }
        return new DamageReductionMaxModifier(rank(), this, base(), monster(), damagetype(), resistance);
    }

    @Override
    public String string() {
        double min = value(0);
        double max = value(steps() - 1);

        String valuestr = String.format("%s%.00f", (min < 0 ? "-" : (base() ? "" : "+")), Math.abs(min));
        String drstr = (monster() ? "Monster %s Reduction" : "%s Reduction");
        drstr = String.format(drstr, damagetype.string());
        if(steps() > 1) { valuestr = String.format("(%s - %s%.00f)", valuestr, (max < 0 ? "-" : (base() ? "" : "+")), Math.abs(max)); }
        return String.format("%s%% Max %s", valuestr, drstr);
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING, damagetype().name());
        pdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, precision());
        pdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, flat());
        pdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, steps());
    }
}
