package net.mctitan.rpg.spell.projectile;

import net.mctitan.rpg.enums.SpellProjectileType;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.projectile.templates.DamagingBoltProjectile;
import net.mctitan.rpg.util.directional.Location;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class ShockBoltProjectile extends DamagingBoltProjectile {
    public ShockBoltProjectile() {}

    public ShockBoltProjectile(SpellInstance instance, Location location, Vector velocity) {
        super(instance, location, velocity);
    }

    public SpellProjectileType type() { return SpellProjectileType.SHOCK_BOLT; }
    public Particle particle() { return Particle.DUST; }
    public Color color() { return Color.fromRGB(0xEB, 0xDC, 0x00); }
    public Sound sound() { return Sound.ENTITY_LIGHTNING_BOLT_IMPACT; }
    public float volume() { return 0.2f; }
}
