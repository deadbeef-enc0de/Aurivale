package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.enums.ProjectileType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.ProjectileSpeedModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class ProjectileSpeedTemplate extends ModifierTemplate {
    private Operator operator;
    private ProjectileType projectile;
    private double precision;
    private double flat;
    private int steps;

    public ProjectileSpeedTemplate(int rank, boolean base, boolean monster, String id, Operator operator, ProjectileType projectile,
                                   double precision, double flat, int steps) {
        super(rank, base, monster, id);

        this.operator = operator;
        this.projectile = projectile;
        this.precision = precision;
        this.flat = flat;
        this.steps = steps;
    }

    public ProjectileSpeedTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.OPERATOR_KEY, PersistentDataType.STRING)) {
            String operatorstr = pdc.get(Key.OPERATOR_KEY, PersistentDataType.STRING);
            operator = Operator.valueOf(operatorstr);
        }

        if(pdc.has(Key.PROJECTILE_KEY, PersistentDataType.STRING)) {
            String projectilestr = pdc.get(Key.PROJECTILE_KEY, PersistentDataType.STRING);
            projectile = ProjectileType.valueOf(projectilestr);
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

    public ProjectileSpeedTemplate(ConfigurationSection section) {
        super(section);

        this.operator = Operator.valueOf(section.getString("operator"));
        this.projectile = ProjectileType.valueOf(section.getString("projectile"));
        this.precision = section.getDouble("precision");
        this.flat = section.getDouble("flat");
        this.steps = section.getInt("steps");
    }

    public Operator operator() { return operator; }
    public ProjectileType projectile() { return projectile; }
    public double precision() { return precision; }
    public double flat() { return flat; }
    public int steps() { return steps; }

    public double value(int step) {return net.mctitan.rpg.util.Math.round(flat() + precision() * step, precision());}

    @Override
    public Modifier modifier(Random random) {
        double value = flat();
        if (steps() > 1) { value = value(random.nextInt(steps())); }

        return new ProjectileSpeedModifier(rank(), this, base(), monster(), operator(), projectile(), value);
    }

    @Override
    public String string() {
        double min = value(0);
        double max = value(steps() - 1);

        if(operator() == Operator.FLAT) {
            String valuestr = String.format("%s%.02f", operator().string(min, base()), Math.abs(min));
            if(steps() > 1) { valuestr = String.format("(%s - %s%.02f", valuestr, operator().string(max, base()), Math.abs(max)); }
            return String.format("%sx %s Speed", valuestr, projectile().string());
        } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
            String valuestr = String.format("%.00f", Math.abs(min));
            if(steps() > 1) { valuestr = String.format("(%s - %.00f", valuestr, Math.abs(max)); }
            return String.format("%s%% %s %s Speed", valuestr, operator().string(min, base()), projectile().string());
        }

        if(steps() > 1) {
            return String.format("Undefined %s Speed %s (%.03f - %.03f)", projectile().name(), operator().name(), min, max);
        } else {
            return String.format("Undefined %s Speed %s %.03f", projectile().name(), operator().name(), min);
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.PROJECTILE_KEY, PersistentDataType.STRING, projectile().name());
        pdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, precision());
        pdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, flat());
        pdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, steps());
    }
}
