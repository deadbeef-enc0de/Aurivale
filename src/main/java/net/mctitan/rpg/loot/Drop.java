package net.mctitan.rpg.loot;

import net.mctitan.rpg.data.ItemStack;

public interface Drop {
    ItemStack stack(int luck);
}
