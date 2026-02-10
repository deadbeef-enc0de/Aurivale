package net.mctitan.rpg.loot.drops;

import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.crafting.Craftables;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.tables.weighted.WeightedTable;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.enums.ItemType;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.loot.Drop;
import net.mctitan.rpg.util.Math;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class CraftableGroupDrop implements Drop {
    private ArrayList<Craftable> craftables = new ArrayList<>();
    private WeightedTable<ItemType> rarities = new WeightedTable<>();

    public CraftableGroupDrop(List<String> craftableids, ConfigurationSection raritysection) {
        // get craftables to drop
        for(String craftableid : craftableids) {
            Craftable craftable = Craftables.instance().craftable(craftableid);
            if(craftable != null) {
                craftables.add(craftable);
            }
        }

        // get rarity of drop
        rarities.insert(ItemType.NORMAL, raritysection.getInt("normal"));
        rarities.insert(ItemType.MAGIC, raritysection.getInt("magic"));
        rarities.insert(ItemType.RARE, raritysection.getInt("rare"));
    }

    public ItemStack stack(int luck) {
        if(craftables.isEmpty()) {
            return null;
        }

        // setup luck for different parts
        int craftroll = Math.max(luck / 2, 0);
        int enchantroll = Math.max(luck / 2, 0);
        int rarityroll = luck;

        // get base craftable to drop
        int index = Math.nextInt(craftables.size());
        Craftable craftable = craftables.get(index);
        ItemStack stack = craftable.create(craftroll);

        // check for normal, magic, or rare item
        ItemType rarity = rarities.get(rarityroll);
        switch(rarity) {
            // magic item with 2-4 modifiers
            case MAGIC -> {
                EnchanterType.WHISPERING_EMBER.enchanter().enchant(stack, enchantroll);
                while(stack.modifiers(ModifierSlot.PREFIX, ModifierSlot.SUFFIX).size() < 4 && Math.nextDouble() < 0.5) {
                    EnchanterType.CHARM_OF_ENRICHMENT.enchanter().enchant(stack, enchantroll);
                }
            }

            // rare item with 5-8 modifiers
            case RARE -> {
                EnchanterType.CELESTIAL_CATALYST.enchanter().enchant(stack, enchantroll);
                while(stack.modifiers(ModifierSlot.PREFIX, ModifierSlot.SUFFIX).size() < 8 && Math.nextDouble() < 0.5) {
                    EnchanterType.ECHO_OF_ASCENSION.enchanter().enchant(stack, enchantroll);
                }
            }
        }

        return stack;
    }
}
