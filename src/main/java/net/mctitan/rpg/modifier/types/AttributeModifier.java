package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.data.UUID;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.Attribute;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.AttributeTemplate;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class AttributeModifier extends Modifier {
    private Attribute attribute;
    private Operator operator;
    private double value;
    private UUID uuid = new UUID();

    private transient org.bukkit.attribute.AttributeModifier attributemod;

    public AttributeModifier() {} // for YamlSaver

    public AttributeModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                             Attribute attribute, Operator operator, double value) {
        super(rank, template, base, monster);

        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    public AttributeModifier(PersistentDataContainer pdc) {
        super(pdc, new AttributeTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.ATTRIBUTE_KEY, PersistentDataType.STRING)) {
            String attributestr = pdc.get(Key.ATTRIBUTE_KEY, PersistentDataType.STRING);
            attribute = Attribute.valueOf(attributestr);
        }

        if(pdc.has(Key.OPERATOR_KEY, PersistentDataType.STRING)) {
            String operationstr = pdc.get(Key.OPERATOR_KEY, PersistentDataType.STRING);
            operator = Operator.valueOf(operationstr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public Attribute attribute() { return attribute; }
    public Operator operator() { return operator; }
    public NamespacedKey key() { return new NamespacedKey(Aurivale.instance(), uuid().toString()); }
    public double value() { return value; }
    public UUID uuid() { return uuid; }
    public org.bukkit.attribute.AttributeModifier attributemod() {
        if(attributemod == null) {
            double modvalue = value;
            if(operator == Operator.SCALER || operator == Operator.MULTIPLIER) { modvalue /= 100; }
            if(attribute() == Attribute.ATTACK_SPEED && operator() == Operator.FLAT) {
                modvalue = 1 / modvalue;
            }
            attributemod = new org.bukkit.attribute.AttributeModifier(key(), modvalue, operator().operation());
        }

        return attributemod;
    }

    @Override
    public Component text() {
        double localvalue = (attribute() == Attribute.ATTACK_SPEED ? 1 / value() : value());
        String attributename = attribute().string();
        if(monster()) { attributename = String.format("Monster %s", attributename); }

        if(operator() == Operator.FLAT) {
            String format = String.format("%%s%s", attribute().flatformat());
            String valuestr = String.format(format, operator().string(localvalue, base()), Math.abs(localvalue));
            return Component.text(String.format("%s to %s", valuestr, attributename));
        } else if(operator() == Operator.SCALER || operator == Operator.MULTIPLIER) {
            String valuestr = String.format(attribute().percentformat(), Math.abs(value()));
            return Component.text(String.format("%s%% %s %s", valuestr, operator().string(value(), base()), attributename));
        } else if(operator() == Operator.SET) {
            String format = String.format("%%s%s", attribute().flatformat());
            String valuestr = String.format(format, operator().string(localvalue, base()), Math.abs(localvalue));
            return Component.text(String.format("%s is %s", attributename, valuestr));
        }

        return Component.text(String.format("Undefined %s %s %.03f", attributename, operator().name(), value()));
    }

    @Override
    public void apply(Entity entity) {
        entity.addattribute(this);

        switch(attribute()) {
            case ATTACK_SPEED -> { entity.actions().apply(this); }
        }
    }

    @Override
    public void unapply(Entity entity) {
        entity.removeattribute(this);

        switch(attribute()) {
            case ATTACK_SPEED -> { entity.actions().unapply(this); }
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.ATTRIBUTE_KEY, PersistentDataType.STRING, attribute().name());
        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
