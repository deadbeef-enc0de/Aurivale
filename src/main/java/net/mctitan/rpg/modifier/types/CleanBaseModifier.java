package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.CleanBaseTemplate;
import net.mctitan.rpg.util.Key;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CleanBaseModifier extends Modifier {
    public CleanBaseModifier() {} // for YamlSaver

    public CleanBaseModifier(int rank, ModifierTemplate template, boolean base, boolean monster) {
        super(rank, template, base, monster);
    }

    public CleanBaseModifier(PersistentDataContainer pdc) {
        super(pdc, new CleanBaseTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));
    }

    @Override
    public boolean showtext() { return false; }

    @Override
    public Component text() { return Component.text("Clean Base Modifiers"); }

    public void apply(ModifierSlot slot, ItemStack stack) {
        super.apply(slot, stack);

        stack.removemodifiers(ModifierSlot.BASE);
    }

    @Override
    public void apply(Entity entity) {}

    @Override
    public void unapply(Entity entity) {}

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);
    }
}
