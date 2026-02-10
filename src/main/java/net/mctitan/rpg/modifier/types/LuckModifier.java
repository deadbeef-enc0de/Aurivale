package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.LuckType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.LuckTemplate;
import net.mctitan.rpg.util.Key;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class LuckModifier extends Modifier {
    private LuckType lucktype;
    private double value;

    public LuckModifier() {} // for YamlSaver

    public LuckModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                        LuckType lucktype, double value) {
        super(rank, template, base, monster);

        this.lucktype = lucktype;
        this.value = value;
    }

    public LuckModifier(PersistentDataContainer pdc) {
        super(pdc, new LuckTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.LUCK_TYPE_KEY, PersistentDataType.STRING)) {
            String lucktypestr = pdc.get(Key.LUCK_TYPE_KEY, PersistentDataType.STRING);
            lucktype = LuckType.valueOf(lucktypestr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public LuckType lucktype() { return lucktype; }
    public double value() { return value; }

    @Override
    public Component text() {
        // Double Damage
        if(lucktype() == LuckType.DOUBLE_DAMAGE) {
            if(value() < 1) {
                String valuestr = String.format("%.00f", value());
                return Component.text(String.format("%s%% Double Damage Chance", valuestr));
            } else {
                return Component.text("Attacks deal Double Damage");
            }

        // Lucky Damage
        } else if(lucktype() == LuckType.LUCKY_DAMAGE) {
            return Component.text("Attack Damage Lucky");

        // Drop Luck
        } else if(lucktype() == LuckType.DROP_LUCK) {
            String valuestr = String.format("%s%.00f", (base() && value() > 0 ? "+" : ""), value());
            return Component.text(String.format("%s Drop Luck", valuestr));
        }

        return Component.text(String.format("Undefined Luck %s %.03f", lucktype().name(), value()));
    }

    @Override
    public void apply(Entity entity) {
        switch(lucktype()) {
            case LUCKY_DAMAGE, DOUBLE_DAMAGE -> entity.actions().apply(this);
            case DROP_LUCK -> entity.dropluck().apply(this);
        }
    }

    @Override
    public void unapply(Entity entity) {
        switch(lucktype()) {
            case LUCKY_DAMAGE, DOUBLE_DAMAGE -> entity.actions().unapply(this);
            case DROP_LUCK -> entity.dropluck().unapply(this);
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.LUCK_TYPE_KEY, PersistentDataType.STRING, lucktype().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
