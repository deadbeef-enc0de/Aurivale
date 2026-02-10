package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.DamageTemplate;
import net.mctitan.rpg.util.Key;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CooldownModifier extends Modifier {
    private Operator operator;
    private double value;

    public CooldownModifier() {} // for YamlSaver

    public CooldownModifier(int rank, ModifierTemplate template, boolean base, boolean monster, Operator operator, double value) {
        super(rank, template, base, monster);

        this.operator = operator;
        this.value = value;
    }

    public CooldownModifier(PersistentDataContainer pdc) {
        super(pdc, new DamageTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.OPERATOR_KEY, PersistentDataType.STRING)) {
            String operatorstr = pdc.get(Key.OPERATOR_KEY, PersistentDataType.STRING);
            this.operator = Operator.valueOf(operatorstr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            this.value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public Operator operator() { return operator; }
    public double value() { return value; }

    @Override
    public Component text() {
        if(operator() == Operator.FLAT) {
            String valuestr = String.format("%s%.02f", operator().string(value(), base()), Math.abs(value() / 20));
            return Component.text(String.format("%ss Cooldown", valuestr));
        } else if(operator() == Operator.SCALER) {
            String valuestr = String.format("%.0f", value());
            return Component.text(String.format("%s%% Cooldown Recovery", valuestr));
        }

        return Component.text(String.format("Undefined Cooldown %s %.03f", operator(), value()));
    }

    @Override
    public void apply(Entity entity) {
        entity.cooldown().apply(this);
    }

    @Override
    public void unapply(Entity entity) {
        entity.cooldown().unapply(this);
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
