package net.mctitan.rpg.data.action.spell;

import net.mctitan.rpg.enums.SpellType;

public class SpellActionData {
    public SpellType spelltype;
    public int level;

    public SpellActionData(SpellType spelltype, int level) {
        this.spelltype = spelltype;
        this.level = level;
    }
}
