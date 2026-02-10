package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.TriggerType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.TriggerTemplate;
import net.mctitan.rpg.util.Key;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class TriggerModifier extends Modifier{
    private TriggerType triggertype;
    private boolean effect;
    private Modifier modifier;

    public TriggerModifier() {} // for YamlSaver

    public TriggerModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                           TriggerType triggertype, boolean effect, Modifier modifier) {
        super(rank, template, base, monster);

        this.triggertype = triggertype;
        this.effect = effect;
        this.modifier = modifier;
    }

    public TriggerModifier(PersistentDataContainer pdc) {
        super(pdc, new TriggerTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.TRIGGER_TYPE_KEY, PersistentDataType.STRING)) {
            String triggertypestr = pdc.get(Key.TRIGGER_TYPE_KEY, PersistentDataType.STRING);
            triggertype = TriggerType.valueOf(triggertypestr);
        }

        if(pdc.has(Key.EFFECT_KEY, PersistentDataType.BOOLEAN)) {
            effect = pdc.get(Key.EFFECT_KEY, PersistentDataType.BOOLEAN);
        }

        if(pdc.has(Key.MODIFIER_KEY, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer modifierpdc = pdc.get(Key.MODIFIER_KEY, PersistentDataType.TAG_CONTAINER);
            modifier = Modifier.modifier(modifierpdc);
        }
    }

    public TriggerType triggertype() { return triggertype; }
    public boolean effect() { return effect; }
    public Modifier modifier() { return modifier; }

    @Override
    public Component text() {
        return modifier().text().append(Component.text(String.format(" %s %s", triggertype().adverb(), triggertype().action())));
    }

    @Override
    public void apply(Entity entity) { entity.actions().apply(this); }

    @Override
    public void unapply(Entity entity) { entity.actions().unapply(this); }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        PersistentDataContainer modifierpdc = pdc.getAdapterContext().newPersistentDataContainer();
        if(modifier() != null) {
            modifier().save(modifierpdc);
            modifierpdc.set(Key.MODIFIER_CLASS_KEY, PersistentDataType.STRING, modifier.getClass().getSimpleName());
        }

        pdc.set(Key.TRIGGER_TYPE_KEY, PersistentDataType.STRING, triggertype.name());
        pdc.set(Key.EFFECT_KEY, PersistentDataType.BOOLEAN, effect());
        pdc.set(Key.MODIFIER_KEY, PersistentDataType.TAG_CONTAINER, modifierpdc);
    }
}
