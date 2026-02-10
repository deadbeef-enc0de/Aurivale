package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.EnchantmentModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.Text;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class EnchantmentTemplate extends ModifierTemplate {
    private Enchantment enchantment;
    private double precision;
    private double flat;
    private int steps;

    public EnchantmentTemplate(int rank, boolean base, boolean monster, String id, Enchantment enchantment,
                               double precision, double flat, int steps) {
        super(rank, base, monster, id);

        this.enchantment = enchantment;
        this.precision = precision;
        this.flat = flat;
        this.steps = steps;
    }

    public EnchantmentTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.ENCHANTMENT_KEY, PersistentDataType.STRING)) {
            String enchantmentstr = pdc.get(Key.ENCHANTMENT_KEY, PersistentDataType.STRING);
            enchantment = Enchantment.valueOf(enchantmentstr);
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

    public EnchantmentTemplate(ConfigurationSection section) {
        super(section);

        enchantment = Enchantment.valueOf(section.getString("enchantment"));
        this.precision = section.getDouble("precision");
        this.flat = section.getDouble("flat");
        this.steps = section.getInt("steps");
    }

    public Enchantment enchantment() { return enchantment; }
    public double precision() { return precision; }
    public double flat() { return flat; }
    public int steps() { return steps; }

    public double value(int step) {return Math.round(flat() + precision() * step, precision());}

    @Override
    public Modifier modifier(Random random) {
        double value = flat();
        if (steps() > 1) { value = value(random.nextInt(steps())); }

        return new EnchantmentModifier(rank(), this, base(), monster(), enchantment(), value);
    }

    @Override
    public String string() {
        double min = value(0);
        double max = value(steps() - 1);
        String enchantmentstr = enchantment().display();

        if(enchantment().maxlevel() > 1) {
            String valuestr = Text.romanint((int)min);
            if(steps() > 1) { valuestr = String.format("(%s - %s)", valuestr, Text.romanint((int)max)); }
            return String.format("%s %s", enchantmentstr, valuestr);
        }

        return enchantmentstr;
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.ENCHANTMENT_KEY, PersistentDataType.STRING, enchantment().name());
        pdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, precision());
        pdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, flat());
        pdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, steps());
    }
}
