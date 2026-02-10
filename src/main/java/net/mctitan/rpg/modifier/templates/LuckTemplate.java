package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.LuckType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.LuckModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class LuckTemplate extends ModifierTemplate {
    private LuckType lucktype;
    private double precision;
    private double flat;
    private int steps;

    public LuckTemplate(int rank, boolean base, boolean monster, String id, LuckType lucktype,
                        double precision, double flat, int steps) {
        super(rank, base, monster, id);

        this.lucktype = lucktype;
        this.precision = precision;
        this.flat = flat;
        this.steps = steps;
    }

    public LuckTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.LUCK_TYPE_KEY, PersistentDataType.STRING)) {
            String lucktypestr = pdc.get(Key.LUCK_TYPE_KEY, PersistentDataType.STRING);
            lucktype = LuckType.valueOf(lucktypestr);
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

    public LuckTemplate(ConfigurationSection section) {
        super(section);

        this.lucktype = LuckType.valueOf(section.getString("lucktype"));
        this.precision = section.getDouble("precision");
        this.flat = section.getDouble("flat");
        this.steps = section.getInt("steps");
    }

    public LuckType lucktype() { return lucktype; }
    public double precision() { return precision; }
    public double flat() { return flat; }
    public int steps() { return steps; }

    public double value(int step) {return Math.round(flat() + precision() * step, precision());}

    @Override
    public Modifier modifier(Random random) {
        double value = flat();
        if (steps() > 1) { value = value(random.nextInt(steps())); }

        return new LuckModifier(rank(), this, base(), monster(), lucktype(), value);
    }

    @Override
    public String string() {
        double min = value(0);
        double max = value(steps() - 1);

        // Double Damage
        if(lucktype() == LuckType.DOUBLE_DAMAGE) {
            if(min < 1) {
                String valuestr = String.format("%.00f", min);
                if(steps() > 1) { valuestr = String.format("(%s - %.00f)", valuestr, max); }
                return String.format("%s%% Double Damage Chance", valuestr);
            } else {
                return "Attacks deal Double Damage";
            }

        // Lucky Damage
        } else if(lucktype() == LuckType.LUCKY_DAMAGE) {
            return "Attack Damage Lucky";

        // Drop Luck
        } else if(lucktype() == LuckType.DROP_LUCK) {
            String valuestr = String.format("%s%.00f", (base() && min > 0) ? "+" : "", min);
            if(steps() > 1) { valuestr = String.format("(%s - %s%.00f)", valuestr, (base() && max > 0) ? "+" : "", max); }
            return String.format("%s Drop Luck", valuestr);
        }

        return String.format("Undefined Luck %s %.03f", lucktype().name(), min);
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.LUCK_TYPE_KEY, PersistentDataType.STRING, lucktype().name());
        pdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, precision());
        pdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, flat());
        pdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, steps());
    }
}
