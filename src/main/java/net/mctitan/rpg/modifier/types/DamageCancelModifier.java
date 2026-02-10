package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.CancelType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.DamageCancelTemplate;
import net.mctitan.rpg.util.Key;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DamageCancelModifier extends Modifier {
    private CancelType canceltype;
    private double value;

    public DamageCancelModifier() {} // for YamlSaver

    public DamageCancelModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                                CancelType canceltype, double value) {
        super(rank, template, base, monster);

        this.canceltype = canceltype;
        this.value = value;
    }

    public DamageCancelModifier(PersistentDataContainer pdc) {
        super(pdc, new DamageCancelTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.CANCEL_TYPE_KEY, PersistentDataType.STRING)) {
            String canceltypestr = pdc.get(Key.CANCEL_TYPE_KEY, PersistentDataType.STRING);
            this.canceltype = CancelType.valueOf(canceltypestr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            this.value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public CancelType canceltype() { return canceltype; }
    public double value() { return value; }

    @Override
    public Component text() {
        String valuestr = String.format("%.00f", value());
        String typestr = canceltype().string();

        if(value() >= 100) {
            return Component.text(String.format("Deal no %s Damage", typestr));
        } else {
            return Component.text(String.format("Deal %s%% Less %s Damage", valuestr, typestr));
        }
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

        pdc.set(Key.CANCEL_TYPE_KEY, PersistentDataType.STRING, canceltype().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
