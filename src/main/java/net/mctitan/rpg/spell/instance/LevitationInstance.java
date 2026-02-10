package net.mctitan.rpg.spell.instance;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.enums.EffectType;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.type.LevitationSpell;
import net.mctitan.rpg.util.directional.Location;

public class LevitationInstance extends SpellInstance {
    private int effectlevel;

    public LevitationInstance() {}

    public LevitationInstance(int id, int level, int duration, Entity caster, Entity target) {
        super(id, level, duration, caster, target);
    }

    public SpellType type() { return SpellType.LEVITATION; }
    public boolean instant() { return true; }

    public void create() {
        // get spell instance
        LevitationSpell spell = (LevitationSpell)spell();

        // get some default variables for the spell
        effectlevel = spell.level(level());

        // apply caster modifications to the spell
        duration(caster().actions().spell().duration().value(duration()));

        // do the effect
        effect(new Location(caster().bukkitentity().getEyeLocation()));
    }

    public void tick(int tick) {}

    public void effect(Location location) {
        // apply levitation effect
        caster().statuseffects().addexternal(new StatusEffect(EffectType.LEVITATION, effectlevel, duration()));
    }
}
