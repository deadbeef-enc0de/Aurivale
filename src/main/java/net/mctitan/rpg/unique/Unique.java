package net.mctitan.rpg.unique;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.crafting.Craftables;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.enums.ItemType;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.modifier.ModifierTemplate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class Unique {
    private String displayname;
    private Integer modeldata;
    private int weight;
    private Craftable craftable;
    private List<ModifierTemplate> basetemplates = new LinkedList<>();
    private LinkedHashMap<String, ModifierTemplate> templates = new LinkedHashMap<>();

    public Unique(ConfigurationSection section) {
        displayname = section.getString("displayname");
        craftable = Craftables.instance().craftable(section.getString("craftable"));
        if(craftable == null) {
            return;
        }

        if(section.contains("modeldata")) {
            modeldata = section.getInt("modeldata");
        }
        this.weight = section.getInt("weight", 1);

        if(section.contains("modifiers")) {
            ConfigurationSection templatessection = section.getConfigurationSection("modifiers");
            for(String templateid : templatessection.getKeys(false)) {
                ConfigurationSection templatesection = templatessection.getConfigurationSection(templateid);
                boolean base = templatesection.getBoolean("base", false);
                ModifierTemplate template = ModifierTemplate.template(templatesection);

                // if template not found skip, or add it to base or normal modifiers
                if(template == null) { continue; }
                if(base) {
                    basetemplates.add(template);
                } else {
                    templates.put(template.id(), template);
                }
            }
        }
    }

    public static Unique valueOf(String id) { return Uniques.instance().get(id); }

    public String displayname() { return displayname; }
    public Integer modeldata() { return modeldata; }
    public int weight() { return weight; }
    public Craftable craftable() { return craftable; }

    public Set<String> modifierids() { return templates.keySet(); }
    public ModifierTemplate modifier(String id) { return templates.get(id); }

    public ItemStack create() {
        if(craftable == null) {
            return null;
        }

        // get the base stack
        ItemStack stack = craftable.create();
        stack.type(ItemType.UNIQUE);
        stack.addenchantment(Enchantment.mending);

        // set displayname and model data
        stack.displayname(Component.text(displayname).color(NamedTextColor.GOLD));
        if(modeldata != null) { stack.modeldata(modeldata); }

        // add base modifiers to item
        for(ModifierTemplate template : basetemplates) {
            template.modifier().apply(ModifierSlot.BASE, stack);
        }

        // add modifiers to item
        for(ModifierTemplate template : templates.values()) {
            template.modifier().apply(ModifierSlot.UNIQUE, stack);
        }

        return stack;
    }
}
