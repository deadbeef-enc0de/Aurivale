package net.mctitan.rpg.enums;

import org.bukkit.entity.EntityType;

public enum SpellProjectileType {
    BURN_BOLT(),
    FIREBALL(EntityType.FIREBALL),
    FROST_BOLT(),
    HEALING_BOLT(),
    SHOCK_BOLT(),
    ;

    private EntityType type;

    SpellProjectileType() { this(null); }
    SpellProjectileType(EntityType type) { this.type = type; }

    public EntityType type() { return type; }
}
