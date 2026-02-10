package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.enums.TriggerType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.TriggerModifier;
import net.mctitan.rpg.util.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class TriggerTemplate extends ModifierTemplate {
    private TriggerType triggertype;
    private boolean effect;
    private ModifierTemplate template;

    public TriggerTemplate(int rank, boolean base, boolean monster, String id,
                           TriggerType triggertype, boolean effect, ModifierTemplate template) {
        super(rank, base, monster, id);

        this.triggertype = triggertype;
        this.effect = effect;
        this.template = template;
    }

    public TriggerTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.TRIGGER_TYPE_KEY, PersistentDataType.STRING)) {
            String triggertypestr = pdc.get(Key.TRIGGER_TYPE_KEY, PersistentDataType.STRING);
            triggertype = TriggerType.valueOf(triggertypestr);
        }

        if(pdc.has(Key.EFFECT_KEY, PersistentDataType.BOOLEAN)) {
            effect = pdc.get(Key.EFFECT_KEY, PersistentDataType.BOOLEAN);
        }

        if(pdc.has(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer modifierpdc = pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER);
            template = ModifierTemplate.template(modifierpdc);
        }
    }

    public TriggerTemplate(ConfigurationSection section) {
        super(section);

        this.triggertype = TriggerType.valueOf(section.getString("triggertype"));
        this.effect = section.getBoolean("effect");
        this.template = ModifierTemplate.template(section.getConfigurationSection("modifier"));
    }

    public TriggerType triggertype() { return triggertype; }
    public boolean effect() { return effect; }
    public ModifierTemplate template() { return template; }

    @Override
    public Modifier modifier(Random random) {
        Modifier modifier = template().modifier(random);
        return new TriggerModifier(rank(), this, base(), monster(), triggertype(), effect(), modifier);
    }

    @Override
    public String string() {
        return String.format("%s %s %s", template().string(), triggertype().adverb(), triggertype().action());
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        PersistentDataContainer templatepdc = pdc.getAdapterContext().newPersistentDataContainer();
        if(template() != null) {
            template().save(templatepdc);
            templatepdc.set(Key.TEMPLATE_CLASS_KEY, PersistentDataType.STRING, template().getClass().getSimpleName());
        }

        pdc.set(Key.TRIGGER_TYPE_KEY, PersistentDataType.STRING, triggertype.name());
        pdc.set(Key.EFFECT_KEY, PersistentDataType.BOOLEAN, effect());
        pdc.set(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER, templatepdc);
    }
}
