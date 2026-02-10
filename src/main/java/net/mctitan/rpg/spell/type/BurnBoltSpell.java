package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.spell.instance.BurnBoltInstance;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.type.templates.DamagingBoltSpell;

public class BurnBoltSpell extends DamagingBoltSpell {
    public String name() { return "Burn Bolt"; }
    public String configname() { return "burn_bolt"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new BurnBoltInstance(id, level, duration(level), caster, target);
    }
}
