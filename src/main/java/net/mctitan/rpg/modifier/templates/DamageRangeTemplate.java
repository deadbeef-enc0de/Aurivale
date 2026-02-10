package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.DamageRangeModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class DamageRangeTemplate extends ModifierTemplate {
    private DamageType damagetype;

    private double minimumprecision;
    private double minimumflat;
    private int minimumsteps;

    private double maximumprecision;
    private double maximumflat;
    private int maximumsteps;

    public DamageRangeTemplate(int rank, boolean base, boolean monster, String id, DamageType damagetype,
                               double minimumprecision, double minimumflat, int minimumsteps,
                               double maximumprecision, double maximumflat, int maximumsteps) {
        super(rank, base, monster, id);

        this.damagetype = damagetype;

        this.minimumprecision = minimumprecision;
        this.minimumflat = minimumflat;
        this.minimumsteps = minimumsteps;

        this.maximumprecision = maximumprecision;
        this.maximumflat = maximumflat;
        this.maximumsteps = maximumsteps;
    }

    public DamageRangeTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING)) {
            String damagetypestr = pdc.get(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING);
            damagetype = DamageType.valueOf(damagetypestr);
        }

        if(pdc.has(Key.MINIMUM_KEY, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer minimumpdc = pdc.get(Key.MINIMUM_KEY, PersistentDataType.TAG_CONTAINER);

            if(minimumpdc.has(Key.PRECISION_KEY, PersistentDataType.DOUBLE)) {
                minimumprecision = minimumpdc.get(Key.PRECISION_KEY, PersistentDataType.DOUBLE);
            }

            if(minimumpdc.has(Key.FLAT_KEY, PersistentDataType.DOUBLE)) {
                minimumflat = minimumpdc.get(Key.FLAT_KEY, PersistentDataType.DOUBLE);
            }

            if(minimumpdc.has(Key.STEPS_KEY, PersistentDataType.INTEGER)) {
                minimumsteps = minimumpdc.get(Key.STEPS_KEY, PersistentDataType.INTEGER);
            }
        }

        if(pdc.has(Key.MAXIMUM_KEY, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer maximumpdc = pdc.get(Key.MAXIMUM_KEY, PersistentDataType.TAG_CONTAINER);

            if(maximumpdc.has(Key.PRECISION_KEY, PersistentDataType.DOUBLE)) {
                maximumprecision = maximumpdc.get(Key.PRECISION_KEY, PersistentDataType.DOUBLE);
            }

            if(maximumpdc.has(Key.FLAT_KEY, PersistentDataType.DOUBLE)) {
                maximumflat = maximumpdc.get(Key.FLAT_KEY, PersistentDataType.DOUBLE);
            }

            if(maximumpdc.has(Key.STEPS_KEY, PersistentDataType.INTEGER)) {
                maximumsteps = maximumpdc.get(Key.STEPS_KEY, PersistentDataType.INTEGER);
            }
        }
    }

    public DamageRangeTemplate(ConfigurationSection section) {
        super(section);

        damagetype = DamageType.valueOf(section.getString("damagetype"));

        minimumprecision = section.getDouble("minimum.precision");
        minimumflat = section.getDouble("minimum.flat");
        minimumsteps = section.getInt("minimum.steps");

        maximumprecision = section.getDouble("maximum.precision");
        maximumflat = section.getDouble("maximum.flat");
        maximumsteps = section.getInt("maximum.steps");
    }

    public DamageType damagetype() { return damagetype; }

    public double minimumprecision() { return minimumprecision; }
    public double minimumflat() { return minimumflat; }
    public int minimumsteps() { return minimumsteps; }

    public double maximumprecision() { return maximumprecision; }
    public double maximumflat() { return maximumflat; }
    public int maximumsteps() { return maximumsteps; }

    protected double minimum(int step) { return Math.round(minimumflat() + minimumprecision() * step, minimumprecision()); }
    protected double maximum(int step) { return Math.round(maximumflat() + maximumprecision() * step, maximumprecision()); }

    @Override
    public Modifier modifier(Random random) {
        double minimum = minimumflat();
        if(minimumsteps() > 1) { minimum = minimum(random.nextInt(minimumsteps())); }

        double maximum = maximumflat();
        if(maximumsteps() > 1) { maximum = maximum(random.nextInt(maximumsteps())); }

        return new DamageRangeModifier(rank(), this, base(), monster(), minimum, maximum, damagetype());
    }

    @Override
    public String string() {
        double minmindamage = minimum(0);
        double maxmindamage = minimum(minimumsteps() - 1);
        double minmaxdamage = maximum(0);
        double maxmaxdamage = maximum(maximumsteps() - 1);

        String minimumstr = "";
        if(minimumsteps() > 1) {
            minimumstr = String.format("(%.00f-%.00f)", minmindamage, maxmindamage);
        } else {
            minimumstr = String.format("%.00f", minmindamage);
        }

        String maximumstr = "";
        if(maximumsteps() > 1) {
            maximumstr = String.format("(%.00f-%.00f)", minmaxdamage, maxmaxdamage);
        } else {
            maximumstr = String.format("%.00f", minmaxdamage);
        }

        return String.format("%s%s - %s %s Damage",
                base() ? "" : "+",
                minimumstr,
                maximumstr,
                damagetype().string()
        );
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        PersistentDataContainer minimumpdc = pdc.getAdapterContext().newPersistentDataContainer();
        minimumpdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, minimumprecision());
        minimumpdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, minimumflat());
        minimumpdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, minimumsteps());
        pdc.set(Key.MINIMUM_KEY, PersistentDataType.TAG_CONTAINER, minimumpdc);

        PersistentDataContainer maximumpdc = pdc.getAdapterContext().newPersistentDataContainer();
        maximumpdc.set(Key.MAXIMUM_KEY, PersistentDataType.DOUBLE, maximumprecision());
        maximumpdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, maximumflat());
        maximumpdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, maximumsteps());
        pdc.set(Key.MAXIMUM_KEY, PersistentDataType.TAG_CONTAINER, maximumpdc);

        pdc.set(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING, damagetype().name());
    }
}
