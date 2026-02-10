package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.templates.DamageRangeTemplate;
import net.mctitan.rpg.util.Key;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DamageRangeModifier extends Modifier {
    private double minimum;
    private double maximum;
    private DamageType damagetype;

    public DamageRangeModifier() {} // for YamlSaver

    public DamageRangeModifier(int rank, DamageRangeTemplate template, boolean base, boolean monster,
                               double minimum, double maximum, DamageType damagetype) {
        super(rank, template, base, monster);

        this.minimum = minimum;
        this.maximum = maximum;
        this.damagetype = damagetype;
    }

    public DamageRangeModifier(PersistentDataContainer pdc) {
        super(pdc, new DamageRangeTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.MINIMUM_KEY, PersistentDataType.DOUBLE)) {
            minimum = pdc.get(Key.MINIMUM_KEY, PersistentDataType.DOUBLE);
        }

        if(pdc.has(Key.MAXIMUM_KEY, PersistentDataType.DOUBLE)) {
            maximum = pdc.get(Key.MAXIMUM_KEY, PersistentDataType.DOUBLE);
        }

        if(pdc.has(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING)) {
            String damagetypestr = pdc.get(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING);
            damagetype = DamageType.valueOf(damagetypestr);
        }
    }

    public double minimum() { return minimum; }
    public double maximum() { return maximum; }
    public DamageType damageType() { return damagetype; }

    @Override
    public Component text() {
        return Component.text(String.format("%s%.00f - %.00f %s",
                base() ? "" : "+",
                minimum(),
                maximum(),
                damageType().longstring()
        ));
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

        pdc.set(Key.MINIMUM_KEY, PersistentDataType.DOUBLE, minimum());
        pdc.set(Key.MAXIMUM_KEY, PersistentDataType.DOUBLE, maximum());
        pdc.set(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING, damageType().name());
    }
}
