package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.templates.HealingTemplate;
import net.mctitan.rpg.util.Key;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class HealingModifier extends Modifier {
    private Operator operator;
    private double value;

    public HealingModifier() {}

    public HealingModifier(int rank, HealingTemplate template, boolean base, boolean monster,
                        Operator operator, double value) {
        super(rank, template, base, monster);

        this.operator = operator;
        this.value = value;
    }

    public HealingModifier(PersistentDataContainer pdc) {
        super(pdc, new HealingTemplate(pdc.get(Key.TEMPLATE_KEY,  PersistentDataType.TAG_CONTAINER)));

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
            String valuestr = String.format("%.01f", value());
            return Component.text(String.format("Heals %s health", valuestr));
        }

        return Component.text(String.format("Undefined Healing %s %.03f", operator(), value()));
    }

    @Override
    public void apply(Entity entity) {
        switch(operator()) {
            case FLAT -> { entity.heal(value()); }
        }
    }

    @Override
    public void unapply(Entity entity) {
        // nothing to do!
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
