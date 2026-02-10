package net.mctitan.rpg.trading.trades;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.tables.weighted.WeightedTable;
import net.mctitan.rpg.enums.EnchanterType;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.trading.Trade;
import net.mctitan.rpg.trading.TradeInstance;
import net.mctitan.rpg.trading.Trading;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.Configuration;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class CraftableTrade extends Trade implements Logger {
    private static WeightedTable<EnchanterType> normal = new WeightedTable<>();
    private static WeightedTable<EnchanterType> magic = new WeightedTable<>();
    private static WeightedTable<EnchanterType> rare = new WeightedTable<>();

    private Craftable craftable;

    public CraftableTrade(Craftable craftable) { this.craftable = craftable; }

    @Override
    public TradeInstance instance(Random random, int luck) {
        // get the item stack from the craftable
        ItemStack stack = craftable.create(random, luck);

        // run through the magic system
        while(stack.modifiers(ModifierSlot.PREFIX, ModifierSlot.SUFFIX).size() < 8) {
            // get which enchanter table to use
            WeightedTable<EnchanterType> enchanters;
            switch(stack.type()) {
                case NORMAL ->  enchanters = normal;
                case MAGIC -> enchanters = magic;
                case RARE -> enchanters = rare;
                case null, default -> enchanters = null;
            }

            // make sure there is a table
            if(enchanters == null) {
                log(Level.INFO, String.format("CraftableTrade ItemType=%s does not have an enchanter lookup",
                        stack.type().name()
                ));
                break;
            }

            // get the enchanter
            EnchanterType enchantertype = enchanters.get(random);
            if(enchantertype == null) {
                // We hit the stopping point
                break;
            }

            // apply the enchanter
            int enchantingroll = stack.get(Key.CRAFT_VALUE_KEY, PersistentDataType.INTEGER, 0);
            if(!enchantertype.enchanter().enchant(stack, random, enchantingroll)) {
                // check if it works, prevents an infinite magic aug loop
                break;
            }
        }

        // get the value and cost of item then create instance
        int value = Trading.instance().value(stack);
        List<ItemStack> cost = Trading.instance().enchanterstacks(value);
        return new TradeInstance(value, stack, cost);
    }

    // load up the configuration for magic item handling
    static {
        // get configuration
        Configuration config = Aurivale.instance().getConfig("trading");

        // load normal item enchanters
        normal.insert(null, config.getInt("normal_weight"));
        normal.insert(EnchanterType.WHISPERING_EMBER, config.getInt("magic_weight"));
        normal.insert(EnchanterType.CELESTIAL_CATALYST, config.getInt("rare_weight"));

        // load magic item enchanters
        magic.insert(null, config.getInt("magic_done_weight"));
        magic.insert(EnchanterType.CHARM_OF_ENRICHMENT, config.getInt("magic_add_weight"));
        magic.insert(EnchanterType.ASCENDANT_SEAL, config.getInt("magic_upgrade_weight"));

        // load rare item enchanters
        rare.insert(null, config.getInt("rare_done_weight"));
        rare.insert(EnchanterType.ECHO_OF_ASCENSION, config.getInt("rare_add_weight"));
    }
}
