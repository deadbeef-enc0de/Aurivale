package net.mctitan.rpg.loot.drops;

import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.loot.Drop;
import net.mctitan.rpg.unique.Uniques;

public class UniqueDrop implements Drop {
    public ItemStack stack(int luck) {
        return Uniques.instance().get(luck).create();
    }
}
