package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.SpellActivation;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.SpellModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.Text;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class SpellTemplate extends ModifierTemplate {
    private SpellType spelltype;
    private SpellActivation activation;
    private double precision;
    private double flat;
    private int steps;

    public SpellTemplate(int rank, boolean base, boolean monster, String id, SpellType spelltype, SpellActivation activation,
                         double precision, double flat, int steps) {
        super(rank, base, monster, id);

        this.spelltype = spelltype;
        this.activation = activation;
        this.precision = precision;
        this.flat = flat;
        this.steps = steps;
    }

    public SpellTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.SPELL_TYPE_KEY, PersistentDataType.STRING)) {
            String spelltypestr =  pdc.get(Key.SPELL_TYPE_KEY, PersistentDataType.STRING);
            spelltype = SpellType.valueOf(spelltypestr);
        }

        if(pdc.has(Key.ACTIVATION_KEY, PersistentDataType.STRING)) {
            String activationstr = pdc.get(Key.ACTIVATION_KEY, PersistentDataType.STRING);
            activation = SpellActivation.valueOf(activationstr);
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

    public SpellTemplate(ConfigurationSection section) {
        super(section);

        this.spelltype = SpellType.valueOf(section.getString("spelltype"));
        this.activation = SpellActivation.valueOf(section.getString("activation"));
        this.precision = section.getDouble("precision");
        this.flat = section.getDouble("flat");
        this.steps = section.getInt("steps");
    }

    public SpellType spelltype() { return spelltype; }
    public SpellActivation activation() { return activation; }
    public double precision() { return precision; }
    public double flat() { return flat; }
    public int steps() { return steps; }

    public double value(int step) {return Math.round(flat() + precision() * step, precision());}

    @Override
    public Modifier modifier(Random random) {
        double value = flat();
        if(steps() > 1) { value = value(random.nextInt(steps())); }

        return new SpellModifier(rank(), this, base(), monster(), spelltype(), activation(), (int)value);
    }

    @Override
    public String string() {
        String valuestr = Text.romanint((int)value(0));
        if(steps() > 1) { valuestr = String.format("(%s - %s)", valuestr, Text.romanint((int)value(steps()))); }

        if(activation == SpellActivation.CAST) {
            return String.format("Cast %s %s", spelltype.spell().name(), valuestr);
        } else {
            return String.format("%s %s %s", spelltype.spell().name(), valuestr, activation.verbiage());
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.SPELL_TYPE_KEY, PersistentDataType.STRING, spelltype.name());
        pdc.set(Key.ACTIVATION_KEY, PersistentDataType.STRING, activation.name());
        pdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, precision());
        pdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, flat());
        pdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, steps());
    }
}
