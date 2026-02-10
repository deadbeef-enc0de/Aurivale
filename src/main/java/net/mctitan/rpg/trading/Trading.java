package net.mctitan.rpg.trading;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.crafting.Craftables;
import net.mctitan.rpg.crafting.recipes.Recipe;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.RecipeChoice;

import java.util.*;

public class Trading {
    private static final Trading instance = new Trading();
    private Map<Material, Integer> materials = new HashMap<>();
    private List<Material> materialcurrency = new ArrayList<>();
    private TreeMap<EnchanterType, Integer> enchanters = new TreeMap<>();
    private List<EnchanterType> enchantercurrency = new ArrayList<>();
    private Map<Craftable, Integer> craftables = new HashMap<>();
    private Map<String, Profession> professions = new HashMap<>();

    private int tradeuses;
    private double explogbase;

    private Trading() {
        tradeuses = Aurivale.instance().getConfig("trading").getInt("trade_uses");
        explogbase = Aurivale.instance().getConfig("trading").getInt("trade_exp_log_base");
    }

    public static Trading instance() { return instance; }
    public static void initialize() { instance().load(); }

    public int tradeuses() { return tradeuses; }
    public double explogbase() { return explogbase; }

    public void load() {
        Configuration config = Aurivale.instance().getConfig("trading");

        // deal with material values and currency
        ConfigurationSection materialssection = config.getConfigurationSection("materials");
        for(String materialname : materialssection.getKeys(false)) {
            Material material = Material.getMaterial(materialname);
            materials.put(material, materialssection.getInt(materialname));
        }

        for(String materialname : config.getStringList("materials_currency")) {
            Material material = Material.getMaterial(materialname);
            materialcurrency.add(material);
        }

        // deal with enchanter values and currency
        ConfigurationSection enchanterssection = config.getConfigurationSection("enchanters");
        for(String enchantertypename : enchanterssection.getKeys(false)) {
            EnchanterType enchantertype = EnchanterType.valueOf(enchantertypename);
            enchanters.put(enchantertype, enchanterssection.getInt(enchantertypename));
        }

        for(String enchantertypename : config.getStringList("enchanters_currency")) {
            EnchanterType enchantertype = EnchanterType.valueOf(enchantertypename);
            enchantercurrency.add(enchantertype);
        }

        // get craftable values that cannot be calculated automatically
        ConfigurationSection craftablessection = config.getConfigurationSection("craftables");
        for(String craftablename : craftablessection.getKeys(false)) {
            Craftable craftable = Craftables.instance().craftable(craftablename);
            if(craftable == null) { continue; }
            craftables.put(craftable, craftablessection.getInt(craftablename));
        }

        // get professions
        ConfigurationSection professionssection = config.getConfigurationSection("professions");
        for(String professionname : professionssection.getKeys(false)) {
            ConfigurationSection professionsection = professionssection.getConfigurationSection(professionname);
            professions.put(professionname, new Profession(professionsection));
        }
    }

    public Profession profession(Villager.Profession bukkitprofession) {
        String key = bukkitprofession.translationKey();
        String name = key.replace("entity.minecraft.villager.","");
        return profession(name.toLowerCase());
    }

    public Profession profession(String profession) {
        return professions.get(profession);
    }

    public List<Material> materialcurrency() { return new ArrayList<>(materialcurrency); }
    public List<EnchanterType> enchantercurrency() { return new ArrayList<>(enchantercurrency); }

    public int value(Material material) { return materials.get(material); }
    public int value(EnchanterType enchantertype) { return enchanters.get(enchantertype); }

    public Integer value(Modifier modifier) {
        ModifierConfig modconfig = modifier.config();
        if(modconfig == null) { return null; }

        return 300 - modconfig.weight(modifier.rank());
    }

    public int value(Craftable craftable) { return value(craftable.create()); }

    public int value(ItemStack stack) {
        // if the stack is an enchanter, return that value
        if(stack.enchantertype() != null) { return value(stack.enchantertype()); }

        int value = 0;

        // set base value of item stack
        if(stack.craftable() != null && craftables.containsKey(stack.craftable())) {
            // use craftable override
            value = craftables.get(stack.craftable());
        } else if(stack.craftable() != null) {
            // calculate craftable value from cheapest crafting recipe
            int craftingvalue = 0;
            for(Recipe recipe : stack.craftable().recipes()) {
                Map<RecipeChoice.MaterialChoice, Integer> count = recipe.materialcount();
                for(RecipeChoice.MaterialChoice choice : count.keySet()) {
                    Material material = null;
                    int materialvalue = 0;
                    for(Material m : choice.getChoices()) {
                        if(!materials.containsKey(m)) { continue; }
                        int mvalue = materials.get(m);
                        if(mvalue > materialvalue) {
                            material = m;
                            materialvalue = mvalue;
                        }
                    }
                    if(material == null) { continue; }
                    craftingvalue += materialvalue * count.get(choice);
                }
            }
            value = craftingvalue;
        }

        // increase value from modifiers
        int modifiers = 0;
        int modifiervalue = 0;
        for (Modifier modifier : stack.modifiers()) {
            Integer modvalue = value(modifier);
            if(modvalue == null) { continue; }
            ++modifiers;
            modifiervalue += modvalue;
        }

        // scale modifier value by the number of modifiers
        if(modifiers > 1) {
            modifiervalue = (int) (modifiervalue * Math.pow(modifiers, 0.66));
        }
        value += modifiervalue;

        return value;
    }

    public List<ItemStack> enchanterstacks(int value) {
        // build tree map for values
        TreeMap<Integer, ItemStack> values = new TreeMap<>();
        for(EnchanterType enchantertype : enchantercurrency()) {
            values.put(value(enchantertype), enchantertype.enchanter().stack());
        }

        // get the stacks
        return stacks(values, value);
    }

    public List<ItemStack> materialstacks(int value) {
        // build tree map for values
        TreeMap<Integer, ItemStack> values = new TreeMap<>();
        for(Material material : materialcurrency()) {
            values.put(value(material), new ItemStack(material));
        }

        // get the stacks
        return stacks(values, value);
    }

    public List<ItemStack> stacks(TreeMap<Integer, ItemStack> values, int value) {
        Map<ItemStack, Integer> stackvalues = new HashMap<>();
        for(Map.Entry<Integer, ItemStack> entry : values.entrySet()) {
            stackvalues.put(entry.getValue(), entry.getKey());
        }

        List<ItemStack> stacks = new ArrayList<>();
        ItemStack stack;
        int stackvalue;


        // if the value is lower than the lowest one, return 1 of the lowest
        if(values.floorEntry(value) == null) {
            stacks.add(values.firstEntry().getValue().clone());
            return stacks;
        }

        // get first stack
        stack = values.floorEntry(value).getValue();
        stackvalue = stackvalues.get(stack);
        stack.bukkitstack().setAmount(value / stackvalue);
        value -= stack.bukkitstack().getAmount() * stackvalue;
        stacks.add(stack);

        // if there is no more value, return stacks
        if(value <= 0) { return stacks; }

        // check to see if left over value is lower than the lowest
        if(values.floorEntry(value) == null) {
            if(stack == values.firstEntry().getValue()) {
                // if the first stack is the lowest, add one
                stacks.get(0).bukkitstack().setAmount(stacks.get(0).bukkitstack().getAmount() + 1);
            } else {
                // add one of the lowest stack
                stacks.add(values.firstEntry().getValue().clone());
            }
            return stacks;
        }

        // get second stack
        stack = values.floorEntry(value).getValue();
        stackvalue = stackvalues.get(stack);
        stack.bukkitstack().setAmount((int)Math.ceil(1.0 * value / stackvalue));
        stacks.add(stack);

        return stacks;
    }
}
