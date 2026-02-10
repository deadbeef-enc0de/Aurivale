package net.mctitan.rpg.trading;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enums.RandomType;
import org.bukkit.inventory.MerchantRecipe;

import java.util.Random;

public abstract class Trade {
    public MerchantRecipe recipe(Entity entity) {
        TradeInstance instance = instance(entity.random(RandomType.VILLAGER_TRADE), entity.dropluck().dropluck());
        MerchantRecipe recipe = new MerchantRecipe(
                instance.item().bukkitstack(),
                Trading.instance().tradeuses(),
                Trading.instance().tradeuses(),
                false
        );
        recipe.setIngredients(instance.cost().stream().map(ItemStack::bukkitstack).toList());
        double exp = Math.log(instance.value()) / Math.log(Trading.instance().explogbase());
        recipe.setVillagerExperience((int)exp);

        return recipe;
    }

    public abstract TradeInstance instance(Random random, int luck);
}
