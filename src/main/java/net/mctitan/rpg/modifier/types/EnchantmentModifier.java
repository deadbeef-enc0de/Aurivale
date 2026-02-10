package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.EnchantmentTemplate;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Text;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class EnchantmentModifier extends Modifier {
    private Enchantment enchantment;
    private double value;

    public EnchantmentModifier() {} // for YamlSaver

    public EnchantmentModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                               Enchantment enchantment, double value) {
        super(rank, template, base, monster);

        this.enchantment = enchantment;
        this.value = value;
    }

    public EnchantmentModifier(PersistentDataContainer pdc) {
        super(pdc, new EnchantmentTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.ENCHANTMENT_KEY, PersistentDataType.STRING)) {
            String enchantmentstr = pdc.get(Key.ENCHANTMENT_KEY, PersistentDataType.STRING);
            enchantment = Enchantment.valueOf(enchantmentstr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public Enchantment enchantment() { return enchantment; }
    public double value() { return value; }

    @Override
    public Component text() {
        String enchantmentstr = enchantment().display();

        if(enchantment().maxlevel() > 1) {
            String valuestr = Text.romanint((int)value);
            return Component.text(String.format("%s %s", enchantmentstr, valuestr));
        }

        return Component.text(enchantmentstr);
    }

    @Override
    public void apply(ModifierSlot slot, ItemStack stack) {
        super.apply(slot, stack);
        if(enchantment().bukkit() != null) {
            stack.bukkitstack().addUnsafeEnchantment(enchantment().bukkit(), (int)value());
        }
    }

    @Override
    public void unapply(ModifierSlot slot, ItemStack stack) {
        super.unapply(slot, stack);
        if(enchantment().bukkit() != null) {
            stack.bukkitstack().removeEnchantment(enchantment().bukkit());
        }
    }

    @Override
    public void apply(Entity entity) {
        entity.enchantments().apply(this);
    }

    @Override
    public void unapply(Entity entity) {
        entity.enchantments().unapply(this);
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.ENCHANTMENT_KEY, PersistentDataType.STRING, enchantment().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
