package net.mctitan.rpg.modifier;

import net.kyori.adventure.text.Component;
import net.mctitan.data.BasicData;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.util.Key;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Constructor;

public abstract class Modifier extends BasicData {
    private int rank;
    private boolean base;
    private boolean monster;

    private transient ModifierTemplate template;

    /***** Create/Load/Save *****/
    public Modifier() {} // for YamlSaver

    public Modifier(int rank, ModifierTemplate template, boolean base, boolean monster) {
        this.rank = rank;
        this.template = template;
        this.base = base;
        this.monster = monster;
    }

    public Modifier(PersistentDataContainer pdc, ModifierTemplate template) {
        this.template = template;

        if(pdc.has(Key.RANK_KEY, PersistentDataType.INTEGER)) {
            this.rank = pdc.get(Key.RANK_KEY, PersistentDataType.INTEGER);
        }

        if(pdc.has(Key.BASE_KEY, PersistentDataType.BOOLEAN)) {
            this.base = pdc.get(Key.BASE_KEY, PersistentDataType.BOOLEAN);
        }

        if(pdc.has(Key.MONSTER_KEY, PersistentDataType.BOOLEAN)) {
            this.monster = pdc.get(Key.MONSTER_KEY, PersistentDataType.BOOLEAN);
        }
    }

    public static Modifier modifier(PersistentDataContainer pdc) {
        if(!pdc.has(Key.MODIFIER_CLASS_KEY, PersistentDataType.STRING)) {
            return null;
        }

        String modifierclass = pdc.get(Key.MODIFIER_CLASS_KEY, PersistentDataType.STRING);
        String classname = String.format("net.mctitan.rpg.modifier.types.%s", modifierclass);
        try {
            Class<? extends Modifier> clazz = Class.forName(classname).asSubclass(Modifier.class);
            Constructor<? extends Modifier> constructor = clazz.getConstructor(PersistentDataContainer.class);
            return constructor.newInstance(pdc);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void save(PersistentDataContainer pdc) {
        PersistentDataContainer templatepdc = pdc.getAdapterContext().newPersistentDataContainer();
        template.save(templatepdc);

        pdc.set(Key.CLASS_KEY, PersistentDataType.STRING, getClass().getName());
        pdc.set(Key.RANK_KEY, PersistentDataType.INTEGER, rank());
        pdc.set(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER, templatepdc);
        pdc.set(Key.BASE_KEY, PersistentDataType.BOOLEAN, base());
        pdc.set(Key.MONSTER_KEY, PersistentDataType.BOOLEAN, monster());
    }

    /***** Data Element Access *****/
    public int rank() { return rank; }
    public ModifierTemplate rawtemplate() { return template; }
    protected <T extends ModifierTemplate> T template() { return (T) template; }
    public ModifierConfig config() { return Modifiers.instance().config(id()); }
    public boolean base() { return base; }
    public boolean monster() { return monster; }
    public String id() { return template.id(); }

    public void base(boolean base) {
        this.base = base;
        this.template.base(base);
    }

    public boolean showtext() { return true; }
    public abstract Component text();

    /***** Apply/Unapply *****/
    public final void apply(ItemStack stack) { apply(config().slot(), stack); }
    public void apply(ModifierSlot slot, ItemStack stack) { stack.addmodifier(slot, this); }
    public final void unapply(ItemStack stack) { unapply(config().slot(), stack); }
    public void unapply(ModifierSlot slot, ItemStack stack) { stack.removemodifier(slot, this); }
    public abstract void apply(Entity entity);
    public abstract void unapply(Entity entity);

}
