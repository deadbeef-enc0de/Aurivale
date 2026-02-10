package net.mctitan.rpg.spell.instance;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.damage.Damage;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.type.CallLightningSpell;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

public class CallLightningInstance extends SpellInstance {
    private int timer;
    private double radius;
    private int strikes;
    private Damage damage;
    private int laststriketick;

    public CallLightningInstance() {}

    public CallLightningInstance(int id, int level, int duration, Entity caster, Entity target) {
        super(id, level, duration, caster, target);
    }

    private Location randomloc() {
        double dx = radius * (2 * Math.nextDouble() - 1);
        double dz = radius * (2 * Math.nextDouble() - 1);
        Location location = new Location(caster().bukkitentity().getLocation());
        return location.center().add(dx, 0, dz);
    }

    public SpellType type() { return SpellType.CALL_LIGHTNING; }
    public boolean instant() { return false; }

    public void create() {
        // get spell instance
        CallLightningSpell spell = (CallLightningSpell)spell();

        // get some default variables for the spell
        timer = spell.timer(level());
        radius = spell.radius(level());
        strikes = 1;
        damage = caster().actions().spell().damage();
        damage.addflat(spell.damagetype(), spell.mindamage(level()), spell.maxdamage(level()));
        laststriketick = -1;

        // apply caster modifications to the spell
        duration(caster().actions().spell().duration().value(duration()));
        radius = caster().actions().spell().area().value(radius);
        strikes += 2 * caster().enchantments().level(Enchantment.multishot);

        // run at creation
        scheduledloc(new Location(caster().bukkitentity().getLocation()));
        tick(0);
    }

    public void tick(int tick) {
        // make sure the caster is available
        if(bukkitcaster() == null) {
            return;
        }

        // check against timer to see if we should strike
        if(laststriketick == -1 || tick == duration() || ((tick - laststriketick) >= timer)) {
            // save the tick as the last strike
            laststriketick = tick;

            // for each projectile make a strike
            for(int i = 0; i < strikes; ++i) {
                effect(randomloc());
            }
        }
    }

    public void effect(Location location) {
        // get world
        World world = location.world();

        // create lightning effect
        world.strikeLightningEffect(location.bukkit());

        // get all enemies nearby the lightning strike
        for(LivingEntity livingentity : location.bukkit().getNearbyLivingEntities(1.5)) {
            // get entity
            Entity damagee = DataManager.instance().entity(livingentity);

            // make sure there is an entity and it is not the caster
            if(damagee == null || damagee == caster()) {
                continue;
            }

            // get the damage roll
            DamageRoll roll = damage.roll();

            // damage target
            damage(damagee, roll);
        }
    }
}
