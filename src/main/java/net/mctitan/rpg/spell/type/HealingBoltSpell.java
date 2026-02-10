package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.spell.instance.HealingBoltInstance;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.type.templates.BoltSpell;

public class HealingBoltSpell extends BoltSpell {
    private double healingflat;
    private double healingbase;

    @Override
    public void initialize() {
        super.initialize();

        healingflat = section().getDouble("healing.flat");
        healingbase = section().getDouble("healing.base");
    }

    public double healing(int level) { return healingflat * Math.pow(healingbase, level - 1); }

    public String name() { return "Healing Bolt"; }
    public String configname() { return "healing_bolt"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new HealingBoltInstance(id, level, duration(level), caster, target);
    }
}
