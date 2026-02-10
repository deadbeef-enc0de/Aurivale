package net.mctitan.rpg.enums;

import net.mctitan.rpg.util.Key;
import org.bukkit.NamespacedKey;

public enum LoreSlot {
    HEADER(Key.LORE_HEADER_KEY),
    BASE(Key.LORE_BASE_KEY),
    IMPLICIT(Key.LORE_IMPLICIT_KEY),
    PREFIX(Key.LORE_PREFIX_KEY),
    SUFFIX(Key.LORE_SUFFIX_KEY),
    UNIQUE(Key.LORE_UNIQUE_KEY),
    FOOTER(Key.LORE_FOOTER_KEY)
    ;

    private NamespacedKey key;

    LoreSlot(NamespacedKey key) { this.key = key; }

    public NamespacedKey key() { return key; }
}
