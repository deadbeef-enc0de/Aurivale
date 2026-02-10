package net.mctitan.rpg.enums;

public enum CriticalType {
    CRITICAL_CHANCE("Critical Chance"),
    CRITICAL_DAMAGE("Critical Damage"),
    ;

    private String string;

    CriticalType(String string) { this.string = string; }

    public String string() { return string; }
}
