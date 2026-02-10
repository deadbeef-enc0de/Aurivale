package net.mctitan.rpg.enums;

import net.kyori.adventure.text.format.NamedTextColor;

public enum ItemType {
    NORMAL(NamedTextColor.WHITE),
    MAGIC(NamedTextColor.BLUE,2,2),
    RARE(NamedTextColor.YELLOW,4,4),
    UNIQUE(NamedTextColor.GOLD),
    ;

    private NamedTextColor color;
    private int prefixes;
    private int suffixes;

    ItemType(NamedTextColor color) { this(color, 0,0); }
    ItemType(NamedTextColor color, int prefixes, int suffixes) {
        this.color = color;
        this.prefixes = prefixes;
        this.suffixes = suffixes;
    }

    public NamedTextColor color() { return color; }
    public int prefixes() { return prefixes; }
    public int suffixes() { return suffixes; }
}
