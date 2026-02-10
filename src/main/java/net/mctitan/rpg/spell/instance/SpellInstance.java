package net.mctitan.rpg.spell.instance;

import net.mctitan.data.BaseData;
import net.mctitan.data.UUID;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.enums.SpellProjectileType;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.SpellChangedBlock;
import net.mctitan.rpg.spell.Spells;
import net.mctitan.rpg.spell.projectile.*;
import net.mctitan.rpg.spell.type.Spell;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public abstract class SpellInstance extends BaseData implements Logger {
    private HashSet<SpellProjectile> projectiles = new HashSet<>();
    private HashSet<Location> changedblocks = new HashSet<>();
    private int id;
    private UUID caster;
    private UUID target;
    private int level;
    private int duration;
    private int dropluck;

    private Location scheduledloc;
    private int ticks;
    private boolean started;
    private boolean done;

    public SpellInstance() {}

    public SpellInstance(int id, int level, Entity caster) { this(id, level, -1, caster, null); }
    public SpellInstance(int id, int level, Entity caster, Entity target) { this(id, level, -1, caster, null); }
    public SpellInstance(int id, int level, int duration, Entity caster) { this(id, level, duration, caster, null); }
    public SpellInstance(int id, int level, int duration, Entity caster, Entity target) {
        this.id = id;
        this.caster = caster.uuid;
        if(target != null) { this.target = target.uuid; }
        this.level = level;
        this.duration = duration;
        this.dropluck = caster.dropluck().dropluck();

        this.scheduledloc = new Location(caster.bukkitentity().getLocation());
        this.ticks = 0;
        this.started = false;
        this.done = false;
    }

    public void initialize() {
        for(SpellProjectile projectile : projectiles) {
            projectile.initialize(this);
        }
    }

    public int id() { return id; }
    public org.bukkit.entity.Entity bukkitcaster() { return Bukkit.getEntity(caster.uuid()); }
    public Entity caster() { return DataManager.instance().entity(caster); }
    public Entity target() { return (target == null ? null : DataManager.instance().entity(target)); }
    public int level() { return level; }
    public int duration() { return duration; }
    public int dropluck() { return dropluck; }
    public Location scheduledloc() { return scheduledloc; }
    public int ticks() { return ticks; }
    public boolean started() { return started; }
    public boolean done() { return done; }

    public void duration(int duration)  { this.duration = duration; }
    public void scheduledloc(Location scheduledloc) { this.scheduledloc = scheduledloc; }

    public Collection<SpellProjectile> projectiles() { return (Set<SpellProjectile>)projectiles.clone(); }

    public SpellProjectile createprojectile(SpellProjectileType type, Location location, Vector velocity) {
        SpellProjectile ret = null;

        switch(type) {
            case BURN_BOLT -> { ret = new BurnBoltProjectile(this, location, velocity); }
            case FIREBALL -> { ret = new FireballProjectile(this, location, velocity); }
            case FROST_BOLT -> { ret = new FrostBoltProjectile(this, location, velocity); }
            case HEALING_BOLT -> { ret = new HealingBoltProjectile(this, location, velocity); }
            case SHOCK_BOLT ->  { ret = new ShockBoltProjectile(this, location, velocity); }
        }

        if(ret != null) {
            projectiles.add(ret);
        }

        return ret;
    }

    public void removeprojectile(SpellProjectile projectile) {
        if(done) {
            return;
        }

        projectiles.remove(projectile);
    }

    public Collection<Location> changedblocks() { return (Set<Location>)changedblocks.clone(); }

    public SpellChangedBlock getblock(Location location) {
        Location blockloc = location.blockloc();
        return Spells.instance().block(blockloc);
    }

    public SpellChangedBlock getblockorcreate(Location location) {
        Location blockloc = location.blockloc();
        SpellChangedBlock block = getblock(blockloc);
        if(block == null) {
            block = new SpellChangedBlock(blockloc);
            Spells.instance().register(block);
        }

        if(!hasblock(blockloc)) { changedblocks.add(blockloc); }

        return block;
    }

    public boolean hasblock(Location location) {
        Location blockloc = location.blockloc();
        return changedblocks.contains(blockloc);
    }

    public void removeblock(Location location) {
        Location blockloc = location.blockloc();
        changedblocks.remove(blockloc);
    }

    public void damage(Entity damagee, DamageRoll roll) {
        // if the caster is a player, make sure to keep track of that
        Player player = null;
        if(caster().bukkitentity().getType() == EntityType.PLAYER) { player = (Player)caster(); }

        // apply entities defenses
        roll = damagee.defense().apply(roll);
        if(roll.damage() < 0.01) { return; }

        // add target for non-passive mobs
        if(!damagee.monster().passive() && damagee.mob() != null) {
            // send out target event
            EntityTargetEvent targetEvent = new  EntityTargetEvent(
                    damagee.bukkitentity(),
                    caster().bukkitentity(),
                    EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY
            );
            targetEvent.callEvent();

            // if target event is not cancelled, set target
            if(!targetEvent.isCancelled()) {
                damagee.mob().setTarget(caster().bukkitentity());
            }
        }

        // deal the damage to the entity
        if(player != null) { damagee.lastdamage().add(damagee, player, roll.damage(), dropluck()); }
        log(Level.INFO, String.format("%s's %s-%d dealing %.02f damage to %s",
                caster().name(),
                type().spell().name(),
                level(),
                roll.damage(),
                damagee.name()
        ));
        damagee.damage(roll.damage());
    }

    public void start() {
        Bukkit.getRegionScheduler().run(Aurivale.instance(), scheduledloc().bukkit(), task -> {
            create();
            started = true;
        });
    }

    public void run() {
        Bukkit.getRegionScheduler().run(Aurivale.instance(), scheduledloc().bukkit(), task -> {
            tick(++ticks);
        });
    }

    public final void end() {
        Bukkit.getRegionScheduler().run(Aurivale.instance(), scheduledloc().bukkit(), task -> {
            // mark spell instance as done
            done = true;

            // remove all projectiles
            for(SpellProjectile projectile : projectiles()) {
                projectile.remove();
            }

            // revert all changed blocks
            for(Location location : changedblocks()) {
                SpellChangedBlock block = getblock(location);
                if(block != null) { block.revert(this); }
            }
        });
    }

    public void effect(SpellProjectile projectile) { effect(projectile.location()); }

    public Spell spell() { return type().spell(); }

    public abstract SpellType type();
    public abstract boolean instant();
    public abstract void create();
    public abstract void tick(int tick);
    public abstract void effect(Location location);
}
