package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.Conversion;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.DamageConversionTemplate;
import net.mctitan.rpg.util.Key;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DamageConversionModifier extends Modifier {
    private Conversion conversion;
    private DamageType fromtype;
    private DamageType totype;
    private double value;

    public DamageConversionModifier() {} // for YamlSaver

    public DamageConversionModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                                    Conversion conversion, DamageType fromtype, DamageType totype, double value) {
        super(rank, template, base, monster);

        this.conversion = conversion;
        this.fromtype = fromtype;
        this.totype = totype;
        this.value = value;
    }

    public DamageConversionModifier(PersistentDataContainer pdc) {
        super(pdc, new DamageConversionTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.CONVERSION_KEY, PersistentDataType.STRING)) {
            String conversionstr = pdc.get(Key.CONVERSION_KEY, PersistentDataType.STRING);
            conversion = Conversion.valueOf(conversionstr);
        }

        if(pdc.has(Key.FROM_TYPE_KEY, PersistentDataType.STRING)) {
            String damagetypestr = pdc.get(Key.FROM_TYPE_KEY, PersistentDataType.STRING);
            fromtype = DamageType.valueOf(damagetypestr);
        }

        if(pdc.has(Key.TO_TYPE_KEY, PersistentDataType.STRING)) {
            String damagetypestr = pdc.get(Key.TO_TYPE_KEY, PersistentDataType.STRING);
            totype = DamageType.valueOf(damagetypestr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public Conversion conversion() { return conversion; }
    public DamageType fromtype() { return fromtype; }
    public DamageType totype() { return totype; }
    public double value() { return value; }

    @Override
    public Component text() {
        String format = "%s%% %s %s %s";
        if(monster()) { format = "%s%% Monster %s %s %s"; }
        String fromstr = fromtype().longstring();
        String tostr = totype().string();

        String valuestr = String.format("%.00f", value());

        return Component.text(String.format(format,
                valuestr,
                fromstr,
                conversion().action(),
                tostr
        ));
    }

    @Override
    public void apply(Entity entity) {
        switch(conversion()) {
            case DAMAGE, EXTRA_DAMAGE -> entity.actions().apply(this);
            case TAKEN -> entity.defense().conversion().apply(this);
        }
    }

    @Override
    public void unapply(Entity entity) {
        switch(conversion()) {
            case DAMAGE, EXTRA_DAMAGE -> entity.actions().unapply(this);
            case TAKEN -> entity.defense().conversion().unapply(this);
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.CONVERSION_KEY, PersistentDataType.STRING, conversion().name());
        pdc.set(Key.FROM_TYPE_KEY, PersistentDataType.STRING, fromtype().name());
        pdc.set(Key.TO_TYPE_KEY, PersistentDataType.STRING, totype().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
