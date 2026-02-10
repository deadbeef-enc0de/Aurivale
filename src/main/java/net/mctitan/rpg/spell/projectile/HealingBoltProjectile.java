package net.mctitan.rpg.spell.projectile;

import net.mctitan.rpg.enums.SpellProjectileType;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.projectile.templates.BoltProjectile;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class HealingBoltProjectile extends BoltProjectile {
    private double healing;

    public HealingBoltProjectile() {}

    public HealingBoltProjectile(SpellInstance instance, Location location, Vector velocity) {
        super(instance, location, velocity);
    }

    public double healing() { return healing; }
    public void healing(double healing) { this.healing = healing; }

    public SpellProjectileType type() { return SpellProjectileType.HEALING_BOLT; }
    public Particle particle() { return Particle.ELECTRIC_SPARK; }
    public Color color() { return Color.BLACK; }
    public Sound sound() { return Sound.BLOCK_CHAIN_HIT; }
    public float volume() { return 1f; }
}
