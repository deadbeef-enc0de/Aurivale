package net.mctitan.rpg.data.status;

import net.mctitan.data.BasicData;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.util.Math;
import org.bukkit.potion.PotionEffect;

public class StatusEffectInstance extends BasicData {
    private static final boolean SHOW_AMBIENT = Aurivale.instance().getConfig("status").getBoolean("show_status_ambient");
    private static final boolean SHOW_PARTICLES = Aurivale.instance().getConfig("status").getBoolean("show_status_particles");
    private static final boolean SHOW_ICON = Aurivale.instance().getConfig("status").getBoolean("show_status_icon");

    private transient Entity entity;
    private StatusEffect effect;
    private int applied;
    private int random = Math.nextInt();

    public StatusEffectInstance() {} // for YamlSaver

    public StatusEffectInstance(StatusEffect effect, Entity entity) {
        this.entity = entity;
        this.effect = effect;
        this.applied = entity.bukkitentity().getTicksLived();
    }

    public StatusEffect effect() { return effect; }
    public void entity(Entity entity) { this.entity = entity; }

    public int remaining() {
        if(effect().permanent()) {
            return PotionEffect.INFINITE_DURATION;
        }

        return effect.duration() - (entity.bukkitentity().getTicksLived() - applied);
    }

    public PotionEffect bukkit(Entity entity) {
        // potion amplifier starts at 0 for lowest level effect
        return new PotionEffect(effect.effecttype().potion(), remaining(), effect.level() - 1,
                                SHOW_AMBIENT, SHOW_PARTICLES, SHOW_ICON);
    }

    @Override
    public int hashCode() { return random; }
}
