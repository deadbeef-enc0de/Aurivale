package net.mctitan.rpg.enums;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.mctitan.rpg.Aurivale;
import org.bukkit.NamespacedKey;

public enum ModifierSlot {
    BASE("base", NamedTextColor.GRAY, LoreSlot.BASE),
    IMPLICIT("implicit", NamedTextColor.AQUA, LoreSlot.IMPLICIT),
    PREFIX("prefix", NamedTextColor.BLUE, LoreSlot.PREFIX),
    SUFFIX("suffix", NamedTextColor.DARK_GREEN, LoreSlot.SUFFIX),
    UNIQUE("unique", NamedTextColor.GOLD,  LoreSlot.UNIQUE),
    ;

    private String key;
    private TextColor color;
    private LoreSlot loreslot;

    ModifierSlot(String key, TextColor color, LoreSlot loreslot) {
        this.key = key;
        this.color = color;
        this.loreslot = loreslot;
    }

    public String keyname() { return key; }
    public NamespacedKey key() { return new NamespacedKey(Aurivale.instance(), this.key); }
    public TextColor color() { return color; }
    public LoreSlot lore() { return loreslot; }
}
