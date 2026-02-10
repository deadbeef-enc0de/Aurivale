package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.BlockDamageTemplate;
import net.mctitan.rpg.util.Key;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockDamageModifier extends Modifier {
    private Operator operator;
    private double value;

    public BlockDamageModifier() {} // for YamlSaver

    public BlockDamageModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                               Operator operator, double value) {
        super(rank, template, base, monster);

        this.operator = operator;
        this.value = value;
    }

    public BlockDamageModifier(PersistentDataContainer pdc) {
        super(pdc, new BlockDamageTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.OPERATOR_KEY, PersistentDataType.STRING)) {
            String operatorstr = pdc.get(Key.OPERATOR_KEY, PersistentDataType.STRING);
            operator = Operator.valueOf(operatorstr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public Operator operator() { return operator; }
    public double value() { return value; }

    @Override
    public Component text() {
        if(operator == Operator.FLAT) {
            String valuestr = String.format("%s%.00f", operator().string(value(), base()), value());
            return Component.text(String.format("%s%% Blocked Damage", valuestr));
        } else if(operator == Operator.SCALER || operator == Operator.MULTIPLIER) {
            String valuestr = String.format("%.00f", value());
            return Component.text(String.format("%s%% %s Blocked Damage", valuestr, operator().string(value(), base())));
        } else if(operator == Operator.SET) {
            String valuestr = String.format("%s%.00f", operator().string(value(), base()), value());
            return Component.text(String.format("Blocked Damage is %s%%", valuestr));
        }

        return Component.text(String.format("Undefined Block Damage %s %.03f", operator().name(), value()));
    }

    @Override
    public void apply(Entity entity) {
        entity.defense().blocking().apply(this);
    }

    @Override
    public void unapply(Entity entity) {
        entity.defense().blocking().unapply(this);
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
