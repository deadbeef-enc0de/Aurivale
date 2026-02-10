package net.mctitan.rpg.enums;

public enum CraftableGroup {
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    SHIELD,
    WAND,
    SWORD(true),
    BATTLEAXE(true),
    ARROW,
    BOW,
    CROSSBOW,
    TRIDENT(true),
    PICKAXE,
    SHOVEL,
    AXE,
    HOE,
    POTION,
    SPLASH_POTION,
    LINGERING_POTION,
    STORY,
    NONE,
    ;

    boolean melee = false;

    CraftableGroup() { this(false); }
    CraftableGroup(boolean melee) { this.melee = melee; }

    public boolean melee() { return melee; }
}
