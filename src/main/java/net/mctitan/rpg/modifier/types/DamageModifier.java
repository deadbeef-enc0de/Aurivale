package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.DamageTemplate;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DamageModifier extends Modifier {
    private DamageType damagetype;
    private Operator operator;
    private double value;

    public DamageModifier() {} // for YamlSaver

    public DamageModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                          DamageType damagetype, Operator operator, double value) {
        super(rank, template, base, monster);

        this.damagetype = damagetype;
        this.operator = operator;
        this.value = value;
    }

    public DamageModifier(PersistentDataContainer pdc) {
        super(pdc, new DamageTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING)) {
            String damagetypestr = pdc.get(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING);
            this.damagetype = DamageType.valueOf(damagetypestr);
        }

        if(pdc.has(Key.OPERATOR_KEY, PersistentDataType.STRING)) {
            String operatorstr = pdc.get(Key.OPERATOR_KEY, PersistentDataType.STRING);
            this.operator = Operator.valueOf(operatorstr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            this.value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public DamageType damagetype() { return damagetype; }
    public Operator operator() { return operator; }
    public double value() { return value; }

    public DamageRoll roll() {
        if(operator != Operator.FLAT && operator != Operator.SET) {
            throw new IllegalStateException("Can only get damage roll from flat damage");
        }

        // get the damage roll fo this damage modifier
        DamageRoll roll = new DamageRoll();
        roll.add(damagetype, value);
        return roll;
    }

    @Override
    public Component text() {
        if(operator() == Operator.FLAT) {
            String valuestr = String.format("%s%.00f", operator().string(value(), base()), Math.abs(value()));
            return Component.text(String.format("%s %s", damagetype().longstring(), valuestr));
        } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
            String valuestr = String.format("%.00f", Math.abs(value()));
            return Component.text(String.format("%s%% %s %s", valuestr, operator().string(value(), base()), damagetype().longstring()));
        }

        return Component.text(String.format("Undefined Damage %s %s %.03f", damagetype(), operator(), value()));
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

        pdc.set(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING, damagetype().name());
        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
