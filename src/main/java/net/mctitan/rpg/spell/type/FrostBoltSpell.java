package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.spell.instance.FrostBoltInstance;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.type.templates.DamagingBoltSpell;

public class FrostBoltSpell extends DamagingBoltSpell {
    public String name() { return "Frost Bolt"; }
    public String configname() { return "frost_bolt"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new FrostBoltInstance(id, level, duration(level), caster, target);
    }
}
