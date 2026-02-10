package net.mctitan.rpg.enums;

import net.mctitan.rpg.util.Logger;
import org.bukkit.potion.PotionType;

import java.util.logging.Level;

public enum ActionName {
    DEFAULT,
    TRIGGER,
    SPELLS,
    BABY,
    CHARGED_EXPLOSION,
    SLIME_SMALL,
    SLIME_MEDIUM,
    SLIME_LARGE,
    SONIC_BOOM,
    TRIDENT_THROW,
    WITHER_BIRTH,
    WITHER_SKULL,

    // potion action names
    POTION_FIRE_RESISTANCE,
    POTION_HARMING,
    POTION_HEALING,
    POTION_POISON,
    POTION_SLOWNESS,
    POTION_SWIFTNESS,
    POTION_WATER_BREATHING,
    POTION_WEAKNESS,
    ;

    public static ActionName actionname(String name) { return valueOf(name.toUpperCase()); }

    public static ActionName slime(int size) {
        switch(size) {
            case 1 -> { return SLIME_SMALL; }
            case 2 -> { return SLIME_MEDIUM; }
            case 4 -> { return SLIME_LARGE; }
        }
        return SLIME_SMALL;
    }

    public static ActionName potion(PotionType potiontype) {
        try {
            String actionnamestr = String.format("POTION_%s", potiontype.name());
            return ActionName.valueOf(actionnamestr);
        } catch(Exception e) {
            Logger.LOG(Level.WARNING, String.format("Cannot get action name for bukkitpotion=%s", potiontype.name()));
        }

        return null;
    }
}
