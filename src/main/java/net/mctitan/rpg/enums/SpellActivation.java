package net.mctitan.rpg.enums;

public enum SpellActivation {
    // types for spell casting
    CAST, // actual wand casting

    // types for spell activation
    ON_USE("on Use"), // when the item is used
    ON_MELEE_HIT("on Melee Hit"), // when you hit an enemy with an attack
    ON_KILL("on Kill"), // when you kill an enemy
    WHEN_HIT("when Hit"), // when you are hit by an enemy
    ;

    private String verbiage;

    SpellActivation() { this(""); }
    SpellActivation(String verbiage) { this.verbiage = verbiage; }

    public String verbiage() { return verbiage; }
}
