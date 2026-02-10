package net.mctitan.rpg.enums;

import org.bukkit.inventory.EquipmentSlot;

public enum ItemSlot {
    MAIN_HAND(EquipmentSlot.HAND),
    OFF_HAND(EquipmentSlot.OFF_HAND),
    HEAD(EquipmentSlot.HEAD),
    CHEST(EquipmentSlot.CHEST),
    LEGS(EquipmentSlot.LEGS),
    FEET(EquipmentSlot.FEET),
    BODY(EquipmentSlot.BODY),
    PROJECTILE,
    NONE
    ;

    private EquipmentSlot bukkitslot;

    ItemSlot() { this(null); }
    ItemSlot(EquipmentSlot bukkitslot) {
        this.bukkitslot = bukkitslot;
    }

    public EquipmentSlot bukkit() { return bukkitslot; }
}
