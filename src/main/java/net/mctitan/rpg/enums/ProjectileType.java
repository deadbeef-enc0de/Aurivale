package net.mctitan.rpg.enums;

public enum ProjectileType {
    ARROW("Arrow"),
    PROJECTILE("Projectile"),
    ;

    private String string;

    ProjectileType(String string) {
        this.string = string;
    }

    public String string() { return string; }
}
