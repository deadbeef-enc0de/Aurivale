package net.mctitan.rpg.spell.instance;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.type.HealSpell;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.logging.Level;

public class HealInstance extends SpellInstance implements Logger {
    public HealInstance() {}

    public HealInstance(int id, int level, int duration, Entity caster, Entity target) {
        super(id, level, duration, caster, target);
    }

    public SpellType type() { return SpellType.HEAL; }
    public boolean instant() { return true; }

    public void create() {
        effect(new Location(caster().bukkitentity().getEyeLocation()));
    }

    public void tick(int tick) {}

    public void effect(Location location) {
        // get spell instance
        HealSpell spell = (HealSpell)spell();

        // get healing amount
        double healing = Math.nextDouble(spell.minhealing(level()), spell.maxhealing(level()));

        // create some particles to show healing to players
        World world = location.world();
        world.spawnParticle(Particle.HAPPY_VILLAGER, location.bukkit(), 20, 0.35, 0.35 , 0.35, 1);
        world.spawnParticle(Particle.HAPPY_VILLAGER, location.subtract(0, 1, 0).bukkit(), 20, 0.35, 0.35 , 0.35, 1);

        // heal the caster
        log(Level.INFO, String.format("%s's %s-%d healing %.02f health",
                caster().name(),
                type().spell().name(),
                level(),
                healing
        ));
        caster().heal(healing);
    }
}
