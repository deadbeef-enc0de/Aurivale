package net.mctitan.rpg.enums;

public enum TriggerType {
    SNEAKING("While", "Sneaking"),
    NOT_SNEAKING("While", "Not Sneaking"),
    SPRINTING("While", "Sprinting"),
    NOT_SPRINTING("While", "Not Sprinting"),
    BOW_SHOT("On", "Bow Shot"),
    HALF_HEALTH("Below", "Half Health"),
    ;

    private String adverb;
    private String action;
    TriggerType(String adverb, String action) {
        this.adverb = adverb;
        this.action = action;
    }

    public String adverb() { return adverb; }
    public String action() { return action; }
}
