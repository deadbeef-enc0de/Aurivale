package net.mctitan.rpg.spell.projectile;

import net.mctitan.rpg.enums.SpellProjectileType;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.projectile.templates.DamagingBoltProjectile;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class BurnBoltProjectile extends DamagingBoltProjectile {
    public BurnBoltProjectile() {}

    public BurnBoltProjectile(SpellInstance instance, Location location, Vector velocity) {
        super(instance, location, velocity);
    }

    public SpellProjectileType type() { return SpellProjectileType.BURN_BOLT; }
    public Particle particle() { return Particle.DUST; }
    public Color color() { return Color.fromRGB(0xFF, 0x66, 0x33); }
    public Sound sound() { return Sound.BLOCK_FIRE_EXTINGUISH; }
    public float volume() { return 0.2f; }
}
