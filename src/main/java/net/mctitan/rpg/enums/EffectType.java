package net.mctitan.rpg.enums;

import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public enum EffectType {
    // used effect types
    BLINDNESS(PotionEffectType.BLINDNESS, "Inflicts", 1),
    DARKNESS(PotionEffectType.DARKNESS, "Inflicts", 1),
    FIRE_IMMUNITY(PotionEffectType.FIRE_RESISTANCE, "Grants", 1),
    GLOWING(PotionEffectType.GLOWING, "Inflicts", 1),
    HASTE(PotionEffectType.HASTE, "Grants"),
    HUNGER(PotionEffectType.HUNGER, "Inflicts"),
    INVISIBILITY(PotionEffectType.INVISIBILITY, "Grants", 1),
    JUMP(PotionEffectType.JUMP_BOOST, "Grants", 15),
    LEVITATION(PotionEffectType.LEVITATION, "Grants"),
    MINING_FATIGUE(PotionEffectType.MINING_FATIGUE, "Inflicts", 4),
    NAUSEA(PotionEffectType.NAUSEA, "Inflicts", 1),
    NIGHT_VISION(PotionEffectType.NIGHT_VISION, "Grants", 1),
    POISON(PotionEffectType.POISON, "Inflicts", 5),
    REGENERATION(PotionEffectType.REGENERATION, "Grants", 6),
    SLOW(PotionEffectType.SLOWNESS, "Inflicts", 7),
    SPEED(PotionEffectType.SPEED, "Grants"),
    STRENGTH(PotionEffectType.STRENGTH, "Grants"),
    WATER_BREATHING(PotionEffectType.WATER_BREATHING, "Grants", 1),
    WEAKNESS(PotionEffectType.WEAKNESS, "Inflicts"),
    WITHER(PotionEffectType.WITHER, "Inflicts", 6),

    // potential mob only effects
    INFESTED(PotionEffectType.INFESTED, "Grants", 1),
    OOZING(PotionEffectType.OOZING, "Grants", 1),
    WEAVING(PotionEffectType.WEAVING, "Grants", 1),

    // not used effect types
    ABSORPTION(PotionEffectType.ABSORPTION, "Grants"),
    SATURATION(PotionEffectType.SATURATION, "Grants", 1),
    SLOW_FALLING(PotionEffectType.SLOW_FALLING, "Grants", 1),
    ;

    private static final Map<PotionEffectType, EffectType> mapping = new HashMap<>();
    static {
        for(EffectType type : values()) {
            mapping.put(type.potion, type);
        }
    }

    private int maxlevel;
    private PotionEffectType potion;
    private String verb;

    EffectType(PotionEffectType type, String verb) {
        this(type, verb, Integer.MAX_VALUE);
    }

    EffectType(PotionEffectType potion, String verb, int maxlevel) {
        this.potion = potion;
        this.maxlevel = maxlevel;
        this.verb = verb;
    }

    public PotionEffectType potion() { return potion; }
    public int maxlevel() { return maxlevel; };
    public String verb() { return verb; }

    public static EffectType effecttype(PotionEffectType potion) {
        return mapping.get(potion);
    }
}
