package net.mctitan.rpg.loot.drops;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.tables.tagged.TaggedTable;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.loot.Drop;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.Modifiers;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.Math;

import java.util.List;
import java.util.logging.Level;

public class EnchanterDrop implements Drop {
    private static TaggedTable<ModifierConfig> enchantermods = new TaggedTable<>();
    private EnchanterType enchantertype;
    private double modchance;

    public EnchanterDrop(EnchanterType enchantertype) {
        this.enchantertype = enchantertype;
        this.modchance = Aurivale.instance().getConfig("loot").getDouble("enchanter_mod_chance");
    }

    public ItemStack stack(int luck) {
        // get base item stack
        ItemStack stack = enchantertype.enchanter().stack();

        // if enchanter can be modded and the chance is right or if the enchanter is a meta enchanter
        if((enchantertype.modable() && Math.nextDouble() < modchance) || enchantertype.meta()) {
            stack.enchantermod(enchantermods.get(luck));
        }

        return stack;
    }

    // setup enchantermods table
    static {
        for(String modifierid : Modifiers.instance().configs()) {
            try {
                ModifierConfig config = Modifiers.instance().config(modifierid);
                TaggedTable<ModifierTemplate> modtable = config.table();
                enchantermods.insert(config, modtable.total(), modifierid, List.of());
            } catch(Exception e) {
                Logger.LOG(Level.WARNING, String.format("Can't add enchanter mod %s, incomplete", modifierid));
            }
        }
    }
}
