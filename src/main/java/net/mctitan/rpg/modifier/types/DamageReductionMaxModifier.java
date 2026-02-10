package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.DamageReductionMaxTemplate;
import net.mctitan.rpg.util.Key;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DamageReductionMaxModifier extends Modifier {
    private DamageType damagetype;
    private double value;

    public DamageReductionMaxModifier() {} // for YamlSaver

    public DamageReductionMaxModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                                      DamageType damagetype, double value) {
        super(rank, template, base, monster);

        this.damagetype = damagetype;
        this.value = value;
    }

    public DamageReductionMaxModifier(PersistentDataContainer pdc) {
        super(pdc, new DamageReductionMaxTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }

        if(pdc.has(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING)) {
            String damagetypestr = pdc.get(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING);
            damagetype = DamageType.valueOf(damagetypestr);
        }
    }

    public DamageType damagetype() { return damagetype; }
    public double value() { return value; }

    @Override
    public Component text() {
        String valuestr = String.format("%s%.00f", (value() < 0 ? "-" : (base() ? "" : "+")), Math.abs(value()));
        String drstr = (monster() ? "Monster %s Reduction" : "%s Reduction");
        drstr = String.format(drstr, damagetype().string());
        return Component.text(String.format("%s%% Max %s", valuestr, drstr));
    }

    @Override
    public void apply(Entity entity) {
        synchronized (entity) {
            for(DamageType type : damagetype.children()) {
                entity.defense().reductions().reduction(type).apply(this);
            }
        }
    }

    @Override
    public void unapply(Entity entity) {
        synchronized (entity) {
            for(DamageType type : damagetype.children()) {
                entity.defense().reductions().reduction(type).unapply(this);
            }
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING, damagetype().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
