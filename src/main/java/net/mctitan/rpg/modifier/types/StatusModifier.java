package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.data.status.StatusEffectInstance;
import net.mctitan.rpg.enums.EffectType;
import net.mctitan.rpg.enums.StatusType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.templates.StatusTemplate;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Text;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

public class StatusModifier extends Modifier {
    private EffectType effecttype;
    private StatusType statustype;
    private double level;
    private double ticks;
    private double radius;

    private transient StatusEffect statuseffect;

    public StatusModifier() {} // for YamlSaver

    private StatusModifier(int rank, StatusTemplate template, boolean base, boolean monster,
                           EffectType effecttype, StatusType statustype, double level, double ticks, double radius) {
        super(rank, template, base, monster);

        this.effecttype = effecttype;
        this.statustype = statustype;
        this.level = level;
        this.ticks = ticks;
        this.radius = radius;
    }

    public static StatusModifier permanent(int rank, StatusTemplate template, boolean base, boolean monster,
                                           EffectType effecttype, double level) {
        return new StatusModifier(rank, template, base, monster,
                effecttype, StatusType.PERMANENT, level, 0, 0);
    }

    public static StatusModifier temporary(int rank, StatusTemplate template, boolean base, boolean monster,
                                           EffectType effecttype, double level, double ticks) {
        return new StatusModifier(rank, template, base, monster,
                effecttype, StatusType.TEMPORARY, level, ticks, 0);
    }

    public static StatusModifier immunity(int rank, StatusTemplate template, boolean base, boolean monster,
                                          EffectType effecttype) {
        return new StatusModifier(rank, template, base, monster,
                effecttype, StatusType.IMMUNITY, 0, 0, 0);
    }

    public static StatusModifier aura(int rank, StatusTemplate template, boolean base, boolean monster,
                                      EffectType effecttype, double level, double radius) {
        return new StatusModifier(rank, template, base, monster,
                effecttype, StatusType.AURA, level, 0, radius);
    }

    public static StatusModifier instance(StatusEffectInstance instance) {
        StatusTemplate template = StatusTemplate.instance(instance);
        if(instance.remaining() == PotionEffect.INFINITE_DURATION) {
            return permanent(1, template, false, false,
                             instance.effect().effecttype(), instance.effect().level());
        } else {
            return temporary(1, template, false, false,
                             instance.effect().effecttype(), instance.effect().level(), instance.remaining());
        }
    }

    public StatusModifier(PersistentDataContainer pdc) {
        super(pdc, new StatusTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.EFFECT_TYPE_KEY, PersistentDataType.STRING)) {
            String effecttypestr = pdc.get(Key.EFFECT_TYPE_KEY, PersistentDataType.STRING);
            effecttype = EffectType.valueOf(effecttypestr);
        }

        if(pdc.has(Key.STATUS_TYPE_KEY, PersistentDataType.STRING)) {
            String statustypestr = pdc.get(Key.STATUS_TYPE_KEY, PersistentDataType.STRING);
            statustype = StatusType.valueOf(statustypestr);
        }

        if(statustype != StatusType.IMMUNITY && pdc.has(Key.LEVEL_KEY, PersistentDataType.DOUBLE)) {
            level = pdc.get(Key.LEVEL_KEY, PersistentDataType.DOUBLE);
        }

        if(statustype == StatusType.TEMPORARY && pdc.has(Key.TICKS_KEY, PersistentDataType.DOUBLE)) {
            ticks = pdc.get(Key.TICKS_KEY, PersistentDataType.DOUBLE);
        }

        if(statustype == StatusType.AURA && pdc.has(Key.RADIUS_KEY, PersistentDataType.DOUBLE)) {
            radius = pdc.get(Key.RADIUS_KEY, PersistentDataType.DOUBLE);
        }
    }

    public StatusModifier modify(double level, double ticks) {
        switch(statustype) {
            case PERMANENT -> { throw new IllegalStateException("Cannot modify status type=PERMANENT"); }
            case TEMPORARY -> {
                return temporary(rank(), template(), base(), monster(), effecttype(), level, ticks);
            }
            case IMMUNITY -> { throw new IllegalStateException("Cannot modify status type=IMMUNITY"); }
            case AURA -> { throw new IllegalStateException("Cannot modify status type=AURA"); }
            case null, default -> throw new IllegalStateException("Status type is unknown");
        }
    }

    public EffectType effecttype() { return effecttype; }
    public StatusType statustype() { return statustype; }
    public double level() { return level; }
    public double ticks() { return ticks; }
    public double radius() { return radius; }

    public StatusEffect statuseffect() {
        if(statuseffect == null) {
            switch(statustype()) {
                case PERMANENT -> statuseffect = new StatusEffect(effecttype(), (int)level(), true);
                case TEMPORARY -> statuseffect = new StatusEffect(effecttype(), (int)level(), (int)ticks);
            }
        }

        return statuseffect;
    }

    @Override
    public Component text() {
        String format = "Undefined Status {effecttype} {statustype} {level} {tick} {radius}";
        if(statustype() == StatusType.PERMANENT) { format = ""; }
        if(statustype() == StatusType.TEMPORARY) { format = ""; }
        if(statustype() == StatusType.AURA) { format = ""; }

        if(statustype() == StatusType.PERMANENT) { format = "{effecttype} {level}"; }
        else if(statustype() == StatusType.TEMPORARY) { format = "{verb} {effecttype} {level} for {time}"; }
        else if(statustype() == StatusType.IMMUNITY) { format = "{effecttype} Immunity"; }
        else if(statustype() == StatusType.AURA) { format = "{effecttype} {level} {radius} Aura"; }

        String effectstr = Text.enumtoprint(effecttype.name());
        String levelstr = Text.romanint((int)level(), effecttype().maxlevel());
        String timestr = String.format("%.02fs", ticks()/20);
        String radiusstr = String.format("%.01fm", radius());

        return Component.text(format
                .replace("{effecttype}", effectstr)
                .replace("{statustype}", statustype.name())
                .replace("{level}", levelstr)
                .replace("{time}", timestr)
                .replace("{radius}", radiusstr)
                .replace("{verb}", effecttype().verb())
                .replace("  ", " ")
                .trim()
        );
    }

    @Override
    public void apply(Entity entity) {
        switch(statustype()) {
            case PERMANENT -> entity.statuseffects().apply(this);
            case TEMPORARY -> entity.actions().apply(this);
            case IMMUNITY -> entity.defense().immunities().apply(this);
        }
    }

    @Override
    public void unapply(Entity entity) {
        switch(statustype()) {
            case PERMANENT -> entity.statuseffects().unapply(this);
            case TEMPORARY -> entity.actions().unapply(this);
            case IMMUNITY -> entity.defense().immunities().unapply(this);
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.EFFECT_TYPE_KEY, PersistentDataType.STRING, effecttype.name());
        pdc.set(Key.STATUS_TYPE_KEY, PersistentDataType.STRING, statustype.name());
        if(statustype() != StatusType.IMMUNITY) { pdc.set(Key.LEVEL_KEY, PersistentDataType.DOUBLE, level); }
        if(statustype() == StatusType.TEMPORARY) { pdc.set(Key.TICKS_KEY, PersistentDataType.DOUBLE, ticks); }
        if(statustype() == StatusType.AURA) { pdc.set(Key.RADIUS_KEY, PersistentDataType.DOUBLE, radius); }
    }
}
