package net.mctitan.rpg.enums;

public enum Conversion {
    DAMAGE("to"),
    EXTRA_DAMAGE("as extra"),
    TAKEN("taken as"),
    ;

    private String action;

    private Conversion(String action) { this.action = action; }

    public String action() { return action; }
}
