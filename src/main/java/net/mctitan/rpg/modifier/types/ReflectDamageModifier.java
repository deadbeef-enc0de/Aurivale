package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.ReflectDamageTemplate;
import net.mctitan.rpg.util.Key;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ReflectDamageModifier extends Modifier {
    private DamageType damagetype;
    private double value;

    public ReflectDamageModifier() {} // for YamlSaver

    public ReflectDamageModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                                 DamageType damagetype, double value) {
        super(rank, template, base, monster);

        this.damagetype = damagetype;
        this.value = value;
    }

    public ReflectDamageModifier(PersistentDataContainer pdc) {
        super(pdc, new ReflectDamageTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING)) {
            String damagetypestr = pdc.get(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING);
            damagetype = DamageType.valueOf(damagetypestr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public DamageType damagetype() { return damagetype; }
    public double value() { return value; }

    @Override
    public Component text() {
        String valuestr = String.format("%.00f", value());
        return Component.text(String.format("Reflect %s%% %s Damage", valuestr, damagetype().string()));
    }

    @Override
    public void apply(Entity entity) {
        entity.defense().damagereflection().apply(this);
    }

    @Override
    public void unapply(Entity entity) {
        entity.defense().damagereflection().unapply(this);
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.DAMAGE_TYPE_KEY, PersistentDataType.STRING, damagetype().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
