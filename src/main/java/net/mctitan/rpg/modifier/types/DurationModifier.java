package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.templates.DurationTemplate;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DurationModifier extends Modifier {
    private Operator operator;
    private double value;

    DurationModifier() {}

    public DurationModifier(int rank, DurationTemplate template, boolean base, boolean monster,
                            Operator operator, double value) {
        super(rank, template, base, monster);

        this.operator = operator;
        this.value = value;
    }

    public DurationModifier(PersistentDataContainer pdc) {
        super(pdc, new DurationTemplate(pdc.get(Key.TEMPLATE_KEY,  PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.OPERATOR_KEY,  PersistentDataType.STRING)) {
            String operatorstr = pdc.get(Key.OPERATOR_KEY,  PersistentDataType.STRING);
            this.operator = Operator.valueOf(operatorstr);
        }

        if(pdc.has(Key.VALUE_KEY,  PersistentDataType.DOUBLE)) {
            this.value = pdc.get(Key.VALUE_KEY,  PersistentDataType.DOUBLE);
        }
    }

    public Operator operator() { return operator; }
    public double value() { return value; }

    @Override
    public Component text() {
        if(operator() == Operator.FLAT) {
            String valuestr = String.format("%s%.02f", operator().string(value(), base()), Math.abs(value() / 20));
            return Component.text(String.format("%ss Duration", valuestr));
        } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
            String valuestr = String.format("%.00f", Math.abs(value()));
            return Component.text(String.format("%s%% %s Duration", valuestr, operator().string(value(), base())));
        }

        return Component.text(String.format("Undefined Duration %s %.03f", operator(), value()));
    }

    @Override
    public void apply(Entity entity) {
        entity.actions().apply(this);
    }

    @Override
    public void unapply(Entity entity) {
        entity.actions().unapply(this);
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
