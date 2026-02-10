package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.spell.instance.SpellInstance;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public abstract class Spell {
    private ConfigurationSection section;

    private double manacostflat;
    private double manacostbase;

    private double cooldownflat;
    private double cooldownbase;

    private double durationflat;
    private double durationbase;

    public void initialize() {
        section = Aurivale.instance().getConfig("spells").getConfigurationSection(configname());
        manacostflat = section().getDouble("mana.flat");
        manacostbase = section().getDouble("mana.base");
        cooldownflat = section().getDouble("cooldown.flat");
        cooldownbase = section().getDouble("cooldown.base");
        durationflat = section().getDouble("duration.flat", 0);
        durationbase = section().getDouble("duration.base", 0);
    }

    public ConfigurationSection section() { return section; }
    public int manacost(int level) { return (int)Math.round(manacostflat * Math.pow(manacostbase, level - 1)); }
    public int cooldown(int level) { return (int)Math.round(cooldownflat * Math.pow(cooldownbase, level - 1)); }
    public int duration(int level)  { return (int)Math.round(durationflat * Math.pow(durationbase, level - 1)); }

    public SpellInstance instance(int id, boolean cast, int level, Entity caster, Entity target) {
        // create spell instance
        SpellInstance instance = create(id, level, caster, target);

        // casting spells has extra stuff
        if(cast) {
            // handle mana cost
            if(caster instanceof Player player) {
                player.mana().change(-manacost(level));
            }

            // play spell cast sound
            World world = caster.bukkitentity().getWorld();
            world.playSound(caster.bukkitentity().getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 0.4f, 1f);

            // apply cooldown
            
        }

        return instance;
    }

    public abstract String name();
    public abstract String configname();
    public abstract SpellInstance create(int id, int level, Entity caster, Entity target);

    @Override public String toString() { return name(); }
}
