package net.mctitan.rpg.enums;

public enum ScoreboardStat {
    TOTAL_DAMAGE("total_damage"),
    ALL_DAMAGES("all_damages"),
    ATTACK_SPEED("attack_speed"),
    PROJECTILE_SPEED("projectile_speed"),
    CRITICAL("critical"),
    SPELL_0("spell_0"),
    SPELL_1("spell_1"),
    SPELL_2("spell_2"),
    MANA_COST("mana_cost"),
    DEFENSE("defense"),
    HEALTH("health"),
    MANA("mana"),
    SKILLS("skills"),
    DROP_LUCK("drop_luck"),
    ;

    private String key;

    ScoreboardStat(String key) {
        this.key = key;
    }

    public String key() { return key; }

    public static ScoreboardStat spell(int spellnum) {
        switch (spellnum) {
            case 0 -> { return SPELL_0; }
            case 1 -> { return SPELL_1; }
            case 2 -> { return SPELL_2; }
            default -> { return null; }
        }
    }
}
