package net.mctitan.rpg.enums;

public enum CancelType {
    NON_CRIT("Non-Crit"),
    ;

    private String string;
    CancelType(String string) { this.string = string; }

    public String string() { return string; }
}
