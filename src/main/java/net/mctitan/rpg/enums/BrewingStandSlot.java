package net.mctitan.rpg.enums;

import java.util.List;

public enum BrewingStandSlot {
    POTION_LEFT(0),
    POTION_MIDDLE(1),
    POTION_RIGHT(2),
    INGREDIENT(3),
    FUEL(4),
    PLAYER(5),
    OTHER(-1)
    ;

    private int index;

    BrewingStandSlot(int index) { this.index = index; }

    public int index() { return index; }

    public static BrewingStandSlot rawslot(int slot) {
        if(slot < 0) {
            return OTHER;
        }

        if(slot < values().length) {
            return values()[slot];
        }

        return PLAYER;
    }

    public static List<BrewingStandSlot> potions() { return List.of(POTION_LEFT, POTION_MIDDLE, POTION_RIGHT); }
}
