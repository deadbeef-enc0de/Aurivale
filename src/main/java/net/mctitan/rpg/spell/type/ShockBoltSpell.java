package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.spell.instance.ShockBoltInstance;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.type.templates.DamagingBoltSpell;

public class ShockBoltSpell extends DamagingBoltSpell {
    public String name() { return "Shock Bolt"; }
    public String configname() { return "shock_bolt"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new ShockBoltInstance(id, level, duration(level), caster, target);
    }
}
