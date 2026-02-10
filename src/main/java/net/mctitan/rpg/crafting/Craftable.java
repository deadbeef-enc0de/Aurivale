package net.mctitan.rpg.crafting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.crafting.recipes.Recipe;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.tables.tagged.TaggedTable;
import net.mctitan.rpg.enums.*;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.Modifiers;
import net.mctitan.rpg.util.Item;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.Math;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

public class Craftable implements Logger {
    /** ItemStack size */
    private int amount;

    /** Base material type */
    private Material material;

    /** Template of item to show in various spots */
    private ItemStack template;

    /** What to display as the name of the item */
    private String displayname;

    /** Key for registering crafting recipes */
    private NamespacedKey key;

    /** Recipes for crafting the item */
    private List<Recipe> recipes = new LinkedList<>();

    /** Modifiers that exist on all version of this craftable */
    private List<ModifierTemplate> basemodifiers = new LinkedList<>();

    /** Modifiers that can roll with enchanting */
    private TaggedTable<ModifierTemplate> magicmodifiers = new TaggedTable<>();

    private Set<String> magicmodifierkeys = new HashSet<>();

    /** Resource pack model data */
    private Integer modeldata;

    /** Bonus to enchanting */
    private int enchantingbonus;

    /** Which active slot the item uses */
    private ItemSlot itemslot;

    /** Which group of craftables this belongs to */
    private CraftableGroup group;

    /** If the uuid on the item changes whenever modifiers are added/removed */
    private boolean ephemeral;

    /** If this craftable is enchantable */
    private boolean enchantable;

    /** if this craftable should remove output recipes matching its material */
    private Boolean removerecipes;

    /** If the craftable has some metadata set */
    private ConfigurationSection metasection = null;

    public Craftable(ConfigurationSection section) {
        // load basic information
        this.amount = section.contains("amount") ? section.getInt("amount") : 1;
        this.material = section.contains("material") ? Material.valueOf(section.getString("material")) : null;
        this.displayname = section.getString("displayname");
        this.modeldata = section.contains("modeldata") ? section.getInt("modeldata") : null;
        this.enchantingbonus = section.getInt("enchantingbonus", 0);
        this.key = new NamespacedKey(Aurivale.instance(), section.getName());
        this.ephemeral = section.getBoolean("ephemeral", false);
        this.enchantable = section.getBoolean("enchantable", true);

        // check for meta
        if(section.contains("meta")) {
            metasection = section.getConfigurationSection("meta");
        }

        // get item slot and group
        itemslot = null;
        group = null;
        String searchstr = section.getName();
        do {
            ConfigurationSection search = Aurivale.instance().getConfig("craftables").getConfigurationSection(searchstr);
            if(itemslot == null && search.contains("itemslot")) { itemslot = ItemSlot.valueOf(search.getString("itemslot")); }
            if(group == null && search.contains("group")) { group = CraftableGroup.valueOf(search.getString("group")); }
            if(removerecipes == null && search.contains("removerecipes")) {  removerecipes = search.getBoolean("removerecipes"); }
            searchstr = search.getString("parent", null);
        } while(searchstr != null);
        if(itemslot == null) { itemslot = ItemSlot.NONE; }
        if(group == null) { group = CraftableGroup.NONE; }

        // make sure removes recipes is configured true by default if missing
        if(removerecipes == null) { removerecipes = true; }

        // make sure the item is valid
        if(amount > 0 && material != null) {
            // load the modifiers for this craftable
            loadmodifiers();

            // set up the template item
            this.template = Item.cleanstack(this.material, this.amount);
            this.template.displayname(Component.text(this.displayname).color(NamedTextColor.WHITE));
            this.template.craftable(this);
            for (ModifierTemplate template : basemodifiers) {
                Component templatelore = Component.text(template.string())
                        .color(ModifierSlot.BASE.color())
                        .decoration(TextDecoration.ITALIC, false);
                this.template.addlore(LoreSlot.BASE, templatelore);
            }
            if(modeldata != null) {
                this.template.modeldata(modeldata);
            }

            // load recipes for this craftable
            loadrecipes();
        }
    }

    public static Craftable valueOf(String keyname) { return Craftables.instance().craftable(keyname); }

    private void loadmodifiers() { loadmodifiers(key.getKey()); }

    private void loadmodifiers(String configname) {
        ConfigurationSection configsection = Aurivale.instance().getConfig("craftables").getConfigurationSection(configname);
        if(configsection == null) {
            log(Level.WARNING, "Could not find in craftable config section="+configname);
        }
        loadmodifiers(configsection);
    }

    private void loadmodifiers(ConfigurationSection section) {
        // load parent modifiers first
        if(section.contains("parent")) {
            loadmodifiers(section.getString("parent"));
        }

        // add base modifier templates
        if(section.contains("base_modifiers")) {
            ConfigurationSection basesection = section.getConfigurationSection("base_modifiers");
            for(String basemodifiername : basesection.getKeys(false)) {
                ConfigurationSection templatesection = basesection.getConfigurationSection(basemodifiername);
                ModifierTemplate template = ModifierTemplate.template(templatesection);
                if(template != null) {
                    basemodifiers.add(template);
                }
            }
        }

        // add magic modifiers templates
        if(section.contains("magic_modifiers")) {
            for(String magicmodifiername : section.getStringList("magic_modifiers")) {
                ModifierConfig magicmodifier = Modifiers.instance().config(magicmodifiername);
                if(magicmodifier != null) {
                    log(Level.INFO, String.format("  added magic modifier="+magicmodifiername));
                    magicmodifiers.add(magicmodifier.table());
                    magicmodifierkeys.add(magicmodifier.id());
                } else {
                    log(Level.WARNING, "Could not find magicmodifier="+magicmodifiername);
                }
            }
        }
    }

    private void loadrecipes() {
        // get the craftable section
        ConfigurationSection craftables = Aurivale.instance().getConfig("craftables");

        // if the craftable doesn't have a recipes setion, don't load anything
        if(!craftables.getConfigurationSection(key().getKey()).contains("recipes")) {
            return;
        }

        // We need to go through this craftable and all parents to build a recipe section
        ConfigurationSection fullrecipessection = new MemoryConfiguration();
        String currentkey = key().getKey();
        do {
            // get the current craftable section
            ConfigurationSection craftablesection = craftables.getConfigurationSection(currentkey);

            // Make sure it has a recipes section
            if(craftablesection.contains("recipes")) {
                ConfigurationSection recipessection = craftablesection.getConfigurationSection("recipes");

                // go through each recipe in the recipes
                for(String recipekey : recipessection.getKeys(false)) {
                    ConfigurationSection recipesection = recipessection.getConfigurationSection(recipekey);

                    // if the combined recipes doesn't have this one, just put it in and mve on
                    if(!fullrecipessection.contains(recipekey)) {
                        fullrecipessection.set(recipekey, recipesection);
                        continue;
                    }
                    ConfigurationSection combinedsection = fullrecipessection.getConfigurationSection(recipekey);

                    // copy the type if it is missing
                    if(!combinedsection.contains("type") && recipesection.contains("type")) {
                        combinedsection.set("type", recipesection.getString("type"));
                    }

                    // copy the shape if it is missing
                    if(!combinedsection.contains("shape") && recipesection.contains("shape")) {
                        combinedsection.set("shape", recipesection.getStringList("shape"));
                    }

                    // add any missing materials
                    if(recipesection.contains("materials")) {
                        for (String materialchar : recipesection.getConfigurationSection("materials").getKeys(false)) {
                            String materialkey = String.format("materials.%s", materialchar);
                            if (!combinedsection.contains(materialkey)) {
                                combinedsection.set(materialkey, recipesection.getStringList(materialkey));
                            }
                        }
                    }
                }
            }

            currentkey = craftablesection.getString("parent");
        } while(currentkey != null && craftables.contains(currentkey));

        // take built recipes configuration and create the recipes
        for(String recipename : fullrecipessection.getKeys(false)) {
            ConfigurationSection recipesection = fullrecipessection.getConfigurationSection(recipename);
            String classname = String.format("net.mctitan.rpg.crafting.recipes.%s", recipesection.getString("type"));
            try {
                Class<? extends Recipe> clazz = Class.forName(classname).asSubclass(Recipe.class);
                Constructor<? extends Recipe> constructor = clazz.getConstructor(Craftable.class, ConfigurationSection.class);
                Recipe recipe = constructor.newInstance(this, recipesection);
                recipes.add(recipe);
            } catch(ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log(Level.WARNING, "Could not initialize recipe="+recipename+" for craftable="+key().getKey());
                e.printStackTrace();
            }
        }
    }

    public ItemStack template() { return template; }
    public String displayname() { return displayname; }
    public NamespacedKey key() { return key; }
    public List<Recipe> recipes() { return recipes; }
    public TaggedTable<ModifierTemplate> magicmodifiers() { return magicmodifiers; }
    public Set<String> maigcmodifierkeys() { return Set.copyOf(magicmodifierkeys); }
    public int enchantingbonus() { return enchantingbonus; }
    public ItemSlot itemslot() { return itemslot; }
    public CraftableGroup group() { return group; }
    public boolean ephemeral() { return ephemeral; }
    public boolean removerecipes() { return removerecipes; }

    public ItemStack create() { return create(0); }
    public ItemStack create(int craft) { return create(Math.random(), craft); }
    public ItemStack create(Random random, int craft) {
        ItemStack stack = Item.cleanstack(this.material, this.amount);
        stack.craftable(this);
        stack.enchantable(enchantable);
        stack.type(ItemType.NORMAL);
        stack.displayname(Component.text(this.displayname).color(NamedTextColor.WHITE));

        // set the metadata if it exists
        if(metasection != null) {
            setmetadata(stack);
        }

        // only add crafting value if the item is not ephemeral
        if(!ephemeral()) {
            stack.set(Key.CRAFT_VALUE_KEY, PersistentDataType.INTEGER, craft);
        }

        // if there are base modifiers, add them
        if(!basemodifiers.isEmpty()) {
            stack.setuuid(); // set the uuid before adding modifiers
            for(ModifierTemplate template : basemodifiers) {
                template.modifier(random).apply(ModifierSlot.BASE, stack);
            }
        }

        // set model data if it exists
        if(modeldata != null) {
            stack.modeldata(modeldata);
        }

        return stack;
    }

    private void setmetadata(ItemStack stack) {
        // get the raw metadata
        ItemMeta meta = stack.bukkitstack().getItemMeta();

        // get what type of metadata we are dealing with
        // the implementations are custom and used sparingly
        String type = metasection.getString("type");
        if(type == null) {
            return;
        }

        // potion type
        if(type.equals("potion")) {
            PotionMeta potionmeta = (PotionMeta)meta;
            try {
                Color color = (Color) Color.class.getField(metasection.getString("color")).get(null);
                potionmeta.setColor(color);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log(Level.WARNING, String.format("Could not set potion color=%s for craftable=%s", metasection.getString("color"), key().getKey()));
            }
        }

        stack.bukkitstack().setItemMeta(meta);
    }
}
