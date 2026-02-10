package net.mctitan.rpg.enums;

import java.util.Arrays;
import java.util.List;

public enum SkillType {
    // Individual Skills
    BREWING("brew", "brewed"),
    CRAFTING("craft", "crafted"),
    ENCHANTING("enchant", "enchanted"),

    // Grouped Skills
    ALL_SKILLS("all", BREWING, CRAFTING, ENCHANTING),
    ;

    private String noun;
    private String verb;
    private List<SkillType> children;

    private SkillType(String noun, String verb) {
        this.noun = noun;
        this.verb = verb;
        this.children = null;
    }

    private SkillType(String noun, SkillType... children) {
        this(noun, "");

        this.children = Arrays.asList(children);
    }

    public String noun() { return noun; }
    public String verb() { return verb; }

    public boolean haschildren() { return children != null; }

    public List<SkillType> children() {
        if(children == null) {
            return List.of(this);
        }

        return children;
    }
}
