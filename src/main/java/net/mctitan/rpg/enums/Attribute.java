package net.mctitan.rpg.enums;

import net.mctitan.rpg.util.Text;

public enum Attribute {
    ATTACK_SPEED(org.bukkit.attribute.Attribute.ATTACK_SPEED),
    BREAK_SPEED(org.bukkit.attribute.Attribute.BLOCK_BREAK_SPEED),
    BLOCK_RANGE(org.bukkit.attribute.Attribute.BLOCK_INTERACTION_RANGE),
    ENTITY_RANGE(org.bukkit.attribute.Attribute.ENTITY_INTERACTION_RANGE),
    FALL_DAMAGE(org.bukkit.attribute.Attribute.FALL_DAMAGE_MULTIPLIER),
    MAX_HEALTH(org.bukkit.attribute.Attribute.MAX_HEALTH, "%.00f"),
    MOVEMENT_EFFICIENCY(org.bukkit.attribute.Attribute.MOVEMENT_EFFICIENCY),
    MOVEMENT_SPEED(org.bukkit.attribute.Attribute.MOVEMENT_SPEED),
    SAFE_FALL_DISTANCE(org.bukkit.attribute.Attribute.SAFE_FALL_DISTANCE, "%.00f"),
    SNEAKING_SPEED(org.bukkit.attribute.Attribute.SNEAKING_SPEED),
    STEP_HEIGHT(org.bukkit.attribute.Attribute.STEP_HEIGHT, "%.01f"),
    ;

    private org.bukkit.attribute.Attribute attribute;
    private String string;
    private String flatformat;
    private String percentformat;

    Attribute(org.bukkit.attribute.Attribute attribute) { this(attribute, "%.02f", "%.00f"); }
    Attribute(org.bukkit.attribute.Attribute attribute, String flatformat) { this(attribute, flatformat, "%.00f"); }
    Attribute(org.bukkit.attribute.Attribute attribute, String flatformat, String percentformat) {
        this.attribute = attribute;
        this.string = Text.enumtoprint(name());
        this.flatformat = flatformat;
        this.percentformat = percentformat;
    }

    public org.bukkit.attribute.Attribute attribute() { return attribute; }
    public String string() { return string; }
    public String flatformat() { return flatformat; }
    public String percentformat() { return percentformat; }
}
