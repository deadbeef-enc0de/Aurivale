package net.mctitan.rpg.pack;

import net.mctitan.rpg.modifier.Modifier;
import org.bukkit.Color;

import java.util.LinkedList;
import java.util.List;

public class PackQuality {
    private final String name;
    private final int groups;
    private final int modifiers;
    private final Color color;
    private List<Modifier> qualitymods;

    public PackQuality(String name, int groups, int modifiers, Color color, List<Modifier> qualitymods) {
        this.name = name;
        this.groups = groups;
        this.modifiers = modifiers;
        this.color = color;
        this.qualitymods = new LinkedList<>(qualitymods);
    }

    public String name() { return name; }
    public int groups() { return groups; }
    public int modifiers() { return modifiers; }
    public Color color() { return color; }
    public List<Modifier> extramods() { return qualitymods; }
}
