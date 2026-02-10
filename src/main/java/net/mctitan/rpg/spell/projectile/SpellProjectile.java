package net.mctitan.rpg.spell.projectile;

import net.mctitan.data.BasicData;
import net.mctitan.data.UUID;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.enums.SpellProjectileType;
import net.mctitan.rpg.spell.Spells;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Projectile;

public abstract class SpellProjectile extends BasicData {
    private UUID projectile = null;
    private Location initial;
    private Location lastlocation;
    private Vector velocity;

    private transient SpellInstance instance = null;

    public SpellProjectile() {}

    public SpellProjectile(SpellInstance instance, Location location, Vector velocity) {
        this.initial = location;
        this.velocity = velocity;

        this.instance = instance;
    }

    public void initialize(SpellInstance instance) {
        // set the instance
        this.instance = instance;

        // register this projectile with Spells
        Spells.instance().register(this);
    }

    public UUID uuid() { return projectile; }
    public SpellInstance instance() { return instance; }
    public Location initial() { return initial; }
    public Location location() { return (projectile() == null ? null : new Location(projectile().getLocation())); }
    public Location lastlocation() { return lastlocation; }
    public Vector velocity() { return velocity; }

    public void lastlocation(Location location) { this.lastlocation = location; }

    public Projectile projectile() {
        if(this.projectile == null) {
            return null;
        }
        return (Projectile)Bukkit.getServer().getEntity(this.projectile.uuid());
    }

    protected void projectile(Projectile projectile) {
        this.projectile = new UUID(projectile.getUniqueId());

        // register the projectile
        Spells.instance().register(this);
    }

    public void remove() {
        if(projectile() != null) {
            projectile().getScheduler().run(Aurivale.instance(), task -> {
                projectile().remove();
            }, null);
        }

        // unregister this projectile with Spells and SpellInstance
        Spells.instance().unregister(this);
        instance.removeprojectile(this);
    }

    public void effect() {
        if(instance == null) {
            return;
        }

        instance.effect(this);
    }

    public abstract void create();
    public abstract void tick(int tick);
    public abstract SpellProjectileType type();
}
