package net.mctitan.rpg.spell.projectile;

import net.mctitan.rpg.enums.SpellProjectileType;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.projectile.templates.DamagingBoltProjectile;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class FrostBoltProjectile extends DamagingBoltProjectile {
    public FrostBoltProjectile() {}

    public FrostBoltProjectile(SpellInstance instance, Location location, Vector velocity) {
        super(instance, location, velocity);
    }

    public SpellProjectileType type() { return SpellProjectileType.FROST_BOLT; }
    public Particle particle() { return Particle.DUST; }
    public Color color() { return Color.fromRGB(0xAB, 0xDB, 0xEC); }
    public Sound sound() { return Sound.BLOCK_AMETHYST_CLUSTER_HIT; }
    public float volume() { return 1.0f; }
}
