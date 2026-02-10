package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.templates.AreaTemplate;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class AreaModifier extends Modifier {
    private Operator operator;
    private double value;

    public AreaModifier() {}

    public AreaModifier(int rank, AreaTemplate template, boolean base, boolean monster,
                         Operator operator, double value) {
        super(rank, template, base, monster);

        this.operator = operator;
        this.value = value;
    }

    public AreaModifier(PersistentDataContainer pdc) {
        super(pdc, new AreaTemplate(pdc.get(Key.TEMPLATE_KEY,  PersistentDataType.TAG_CONTAINER)));

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
            String valuestr = String.format("%s%.01f", operator().string(value(), base()), net.mctitan.rpg.util.Math.abs(value()));
            return Component.text(String.format("%sm Area", valuestr));
        } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
            String valuestr = String.format("%.00f", Math.abs(value()));
            return Component.text(String.format("%s%% %s Area", valuestr, operator().string(value(), base())));
        }

        return Component.text(String.format("Undefined Area %s %.03f", operator(), value()));
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
