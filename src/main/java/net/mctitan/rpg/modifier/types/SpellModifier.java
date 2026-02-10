package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.SpellActivation;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.SpellTemplate;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Text;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SpellModifier extends Modifier {
    private SpellType spelltype;
    private SpellActivation activation;
    private int level;

    public SpellModifier() {}

    public SpellModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                         SpellType spelltype, SpellActivation activation, int level) {
        super(rank, template, base, monster);

        this.spelltype = spelltype;
        this.activation = activation;
        this.level = level;
    }

    public SpellModifier(PersistentDataContainer pdc) {
        super(pdc, new SpellTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.SPELL_TYPE_KEY, PersistentDataType.STRING)) {
            String spelltypestr =  pdc.get(Key.SPELL_TYPE_KEY, PersistentDataType.STRING);
            spelltype = SpellType.valueOf(spelltypestr);
        }

        if(pdc.has(Key.ACTIVATION_KEY, PersistentDataType.STRING)) {
            String activationstr = pdc.get(Key.ACTIVATION_KEY, PersistentDataType.STRING);
            activation = SpellActivation.valueOf(activationstr);
        }

        if(pdc.has(Key.LEVEL_KEY, PersistentDataType.INTEGER)) {
            level = pdc.get(Key.LEVEL_KEY, PersistentDataType.INTEGER);
        }
    }

    public SpellType spelltype() { return spelltype; }
    public SpellActivation activation() { return activation; }
    public int level() { return level; }

    @Override
    public Component text() {
        String romanint = Text.romanint(level);
        if(activation == SpellActivation.CAST) {
            return Component.text(String.format("Cast %s %s", spelltype.spell().name(), romanint));
        } else {
            return Component.text(String.format("%s %s %s", spelltype.spell().name(), romanint, activation.verbiage()));
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

        pdc.set(Key.SPELL_TYPE_KEY, PersistentDataType.STRING, spelltype.name());
        pdc.set(Key.ACTIVATION_KEY, PersistentDataType.STRING, activation.name());
        pdc.set(Key.LEVEL_KEY, PersistentDataType.INTEGER, level);
    }
}
