package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.enums.ProjectileType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.ProjectileSpeedTemplate;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ProjectileSpeedModifier extends Modifier {
    private Operator operator;
    private ProjectileType projectile;
    private double value;

    public ProjectileSpeedModifier() {} // for YamlSaver

    public ProjectileSpeedModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                                   Operator operator, ProjectileType projectile, double value) {
        super(rank, template, base, monster);

        this.operator = operator;
        this.projectile = projectile;
        this.value = value;
    }

    public ProjectileSpeedModifier(PersistentDataContainer pdc) {
        super(pdc, new ProjectileSpeedTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.OPERATOR_KEY, PersistentDataType.STRING)) {
            String operatorstr = pdc.get(Key.OPERATOR_KEY, PersistentDataType.STRING);
            operator = Operator.valueOf(operatorstr);
        }

        if(pdc.has(Key.PROJECTILE_KEY, PersistentDataType.STRING)) {
            String projectilestr = pdc.get(Key.PROJECTILE_KEY, PersistentDataType.STRING);
            projectile = ProjectileType.valueOf(projectilestr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public Operator operator() { return operator; }
    public ProjectileType projectile() { return projectile; }
    public double value() { return value; }

    @Override
    public Component text() {
        if(operator() == Operator.FLAT) {
            String valuestr = String.format("%s%.02f", operator().string(value(), base()), Math.abs(value()));
            return Component.text(String.format("%sx %s Speed", valuestr, projectile().string()));
        } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
            String valuestr = String.format("%.00f", Math.abs(value()));
            return Component.text(String.format("%s%% %s %s Speed", valuestr, operator().string(value(), base()), projectile().string()));
        }

        return Component.text(String.format("Undefined %s Speed %s %.03f", projectile().name(), operator().name(), value()));
    }

    @Override
    public void apply(Entity entity) {
        entity.projectiles().speed().apply(this);
    }

    @Override
    public void unapply(Entity entity) {
        entity.projectiles().speed().unapply(this);
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.PROJECTILE_KEY, PersistentDataType.STRING, projectile().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
