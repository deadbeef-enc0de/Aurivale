package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.spell.instance.HealInstance;
import net.mctitan.rpg.spell.instance.SpellInstance;

public class HealSpell extends Spell {
    private double minhealingflat;
    private double minhealingbase;
    private double maxhealingflat;
    private double maxhealingbase;

    @Override
    public void initialize() {
        super.initialize();

        minhealingflat = section().getDouble("healing.minimum.flat");
        minhealingbase = section().getDouble("healing.minimum.base");
        maxhealingflat = section().getDouble("healing.maximum.flat");
        maxhealingbase = section().getDouble("healing.maximum.base");
    }

    public double minhealing(int level) { return minhealingflat * Math.pow(minhealingbase, level - 1); }
    public double maxhealing(int level) { return maxhealingflat * Math.pow(maxhealingbase, level - 1); }

    public String name() { return "Heal"; }
    public String configname() { return "heal"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new HealInstance(id, level, duration(level), caster, target);
    }
}
