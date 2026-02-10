package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.ManaType;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.ManaTemplate;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ManaModifier extends Modifier {
    private ManaType manatype;
    private Operator operator;
    private double value;

    public ManaModifier() {} // for YamlSaver

    public ManaModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                        ManaType manatype, Operator operator, double value) {
        super(rank, template, base, monster);

        this.manatype = manatype;
        this.operator = operator;
        this.value = value;
    }

    public ManaModifier(PersistentDataContainer pdc) {
        super(pdc, new ManaTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.MANA_TYPE_KEY, PersistentDataType.STRING)) {
            String manatypestr = pdc.get(Key.MANA_TYPE_KEY, PersistentDataType.STRING);
            manatype = ManaType.valueOf(manatypestr);
        }

        if(pdc.has(Key.OPERATOR_KEY, PersistentDataType.STRING)) {
            String operatorstr = pdc.get(Key.OPERATOR_KEY, PersistentDataType.STRING);
            operator = Operator.valueOf(operatorstr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public ManaType manatype() { return manatype; }
    public Operator operator() { return operator; }
    public double value() { return value; }

    @Override
    public Component text() {
        // Maximum Mana
        if(manatype() == ManaType.MAX_MANA) {
            if(operator() == Operator.FLAT) {
                String valuestr = String.format("%s%.00f", operator().string(value(), base()), Math.abs(value()));
                return Component.text(String.format("%s Max Mana", valuestr));
            } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
                return Component.text(String.format("%.00f%% %s Max Mana", value(), operator().string(value(), base())));
            } else if(operator() == Operator.SET) {
                String valuestr = String.format("%s%.00f", operator().string(value(), base()), Math.abs(value()));
                return Component.text(String.format("Max Mana is %s", valuestr));
            }

        // Mana Regeneration
        } else if(manatype() == ManaType.MANA_REGEN) {
            if(operator() == Operator.FLAT) {
                String valuestr = String.format("%s%.01f", operator().string(value(), base()), Math.abs(value()));
                return Component.text(String.format("%s Mana Regen", valuestr));
            } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
                return Component.text(String.format("%.00f%% %s Mana Regen", value(), operator().string(value(), base())));
            } else if(operator() == Operator.SET) {
                String valuestr = String.format("%s%.00f", operator().string(value(), base()), Math.abs(value()));
                return Component.text(String.format("Mana Regen is %s", valuestr));
            }
        }

        return Component.text(String.format("Undefined Mana %s %s %.03f", manatype().name(), operator().name(), value()));
    }

    @Override
    public void apply(Entity entity) {
        if(entity instanceof Player player) {
            player.mana().apply(this);
        }
    }

    @Override
    public void unapply(Entity entity) {
        if(entity instanceof Player player) {
            player.mana().unapply(this);
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.MANA_TYPE_KEY, PersistentDataType.STRING, manatype().name());
        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
