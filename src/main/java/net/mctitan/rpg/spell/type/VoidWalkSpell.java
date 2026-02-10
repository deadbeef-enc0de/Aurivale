package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.instance.VoidWalkInstance;

public class VoidWalkSpell extends Spell {
    public String name() { return "Void Walk"; }
    public String configname() { return "void_walk"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new VoidWalkInstance(id, level, caster, target);
    }
}
