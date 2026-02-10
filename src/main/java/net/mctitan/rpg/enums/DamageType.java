package net.mctitan.rpg.enums;

import org.bukkit.ChatColor;

import java.util.LinkedList;
import java.util.List;

public enum DamageType {
    // singular damage types
    PHYSICAL(ChatColor.GRAY, "Physical"),
    BURN(ChatColor.RED, "Burn"),
    FROST(ChatColor.AQUA, "Frost"),
    SHOCK(ChatColor.YELLOW, "Shock"),
    HAVOC(ChatColor.LIGHT_PURPLE, "Havoc"),
    MAGIC(ChatColor.WHITE, "Magic"), // currently has no resistance/reduction

    // aggregate/non-standard damage types
    ALL(ChatColor.DARK_GRAY, "All", "Damage", PHYSICAL, BURN, FROST, SHOCK, HAVOC, MAGIC),
    ELEMENTAL(ChatColor.GOLD, "Elemental", BURN, FROST, SHOCK),
    GENERIC(ChatColor.DARK_GRAY, "Generic"), // used for generic damage that can't be prevented
    NONE(ChatColor.BLACK, "None"),
    ;

    private ChatColor color = null;
    private String string = null;
    private String longstring;
    private List<DamageType> children;

    DamageType(ChatColor color, String string) {
        this.color = color;
        this.string = string;
        this.longstring = String.format("%s Damage", string);
        this.children = null;
    }

    DamageType(ChatColor color, String string, DamageType... children) {
        this(color, string);

        this.children = new LinkedList<>();
        for(DamageType type : children) {
            this.children.add(type);
        }
    }

    DamageType(ChatColor color, String string, String longstring, DamageType... children) {
        this(color, string, children);

        this.longstring = longstring;
    }

    public ChatColor color() { return color; }
    public String string() { return string; }
    public String longstring() { return longstring; }

    public boolean haschildren() { return children != null; }

    public List<DamageType> children() {
        if(children == null) {
            return List.of(this);
        }

        return children;
    }
}
