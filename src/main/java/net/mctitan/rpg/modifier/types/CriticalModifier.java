package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.CriticalType;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.CriticalTemplate;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CriticalModifier extends Modifier {
    private CriticalType criticaltype;
    private Operator operator;
    private double value;

    public CriticalModifier() {} // for YamlSaver

    public CriticalModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                            CriticalType criticaltype, Operator operator, double value) {
        super(rank, template, base, monster);

        this.criticaltype = criticaltype;
        this.operator = operator;
        this.value = value;
    }

    public CriticalModifier(PersistentDataContainer pdc) {
        super(pdc, new CriticalTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.CRITICAL_TYPE_KEY, PersistentDataType.STRING)) {
            String criticaltypestr = pdc.get(Key.CRITICAL_TYPE_KEY, PersistentDataType.STRING);
            criticaltype = CriticalType.valueOf(criticaltypestr);
        }

        if(pdc.has(Key.OPERATOR_KEY, PersistentDataType.STRING)) {
            String operatorstr = pdc.get(Key.OPERATOR_KEY, PersistentDataType.STRING);
            operator = Operator.valueOf(operatorstr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public CriticalType criticaltype() { return criticaltype; }
    public Operator operator() { return operator; }
    public double value() { return value; }

    @Override
    public Component text() {
        String criticaltypestr = criticaltype().string();

        if(operator() == Operator.FLAT) {
            if(criticaltype() == CriticalType.CRITICAL_CHANCE) {
                String valuestr = String.format("%s%.02f", operator().string(value(), base()), Math.abs(value()));
                return Component.text(String.format("%s%% %s", valuestr, criticaltypestr));
            } else if(criticaltype() == CriticalType.CRITICAL_DAMAGE) {
                String valuestr = String.format("%s%.00f", operator().string(value(), base()), Math.abs(value()));
                return Component.text(String.format("%s%% %s", valuestr, criticaltypestr));
            }
        } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
            String valuestr = String.format("%.00f", Math.abs(value()));
            return Component.text(String.format("%s%% %s %s", valuestr, operator().string(value(), base()), criticaltypestr));
        } else if(operator() == Operator.SET) {
            String valuestr = String.format("%.02f", value());
            return Component.text(String.format("%s is %s%%", criticaltypestr, valuestr));
        }

        return Component.text(String.format("Undefined Critical %s %s %.03f", criticaltype().toString(), operator().name(), value()));
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

        pdc.set(Key.CRITICAL_TYPE_KEY, PersistentDataType.STRING, criticaltype().name());
        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
