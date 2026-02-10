package net.mctitan.rpg.handler.general;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.damage.Damage;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.DamageRangeModifier;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.Text;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class EnvironmentalDamage implements Listener, Logger {
    private static final EnvironmentalDamage instance = new EnvironmentalDamage();

    Map<DamageCause, Damage> damages = new HashMap<>();
    Map<DamageCause, Integer> ticks = new HashMap<>();

    private EnvironmentalDamage() {
        log(Level.INFO, "Initializing Environmental Damage");
        Configuration config = Aurivale.instance().getConfig("environmental");
        for(String damagestr : config.getKeys(false)) {
            DamageCause cause = DamageCause.valueOf(damagestr.toUpperCase());
            ConfigurationSection causesection = config.getConfigurationSection(damagestr);
            if(causesection == null) {
                continue;
            }

            int damagetime = causesection.getInt("ticks");
            ConfigurationSection damagesection = causesection.getConfigurationSection("damage");
            if(damagesection == null) {
                continue;
            }

            Modifier modifier = ModifierTemplate.template(damagesection).modifier();
            if(!(modifier instanceof DamageRangeModifier damagerange)) {
                continue;
            }


            String cuasestr = Text.enumtoprint(cause.name());
            Damage damage = new Damage();
            damage.apply(damagerange);
            log(Level.INFO, String.format("%s: %s", cuasestr, PlainTextComponentSerializer.plainText().serialize(damagerange.text())));
            damages.put(cause, damage);
            ticks.put(cause, damagetime);
        }
    }

    public static EnvironmentalDamage instance() { return instance; }

    public Integer ticks(DamageCause cause) { return ticks.getOrDefault(cause, 0); }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // only care about living entities
        if(!(event.getEntity() instanceof org.bukkit.entity.LivingEntity bukkitEntity)) {
            return;
        }

        // we only care about configured environmental types
        DamageCause cause = event.getCause();
        if(!damages.containsKey(cause)) {
            return;
        }

        // get the entity and damage roll
        Entity entity = DataManager.instance().entity(bukkitEntity);
        entity.environment().damage(entity, cause, damages.get(cause));

        // cancel the event
        event.setCancelled(true);
    }

    @EventHandler
    public void onWitherDamage(EntityDamageEvent event) {
        // only care about living entities
        if(!(event.getEntity() instanceof org.bukkit.entity.LivingEntity bukkitEntity)) {
            return;
        }

        // only care about wither and poison damage
        if(event.getCause() != DamageCause.WITHER &&
                event.getCause() != DamageCause.POISON) {
            return;
        }

        // get entity and damage it
        Entity entity = DataManager.instance().entity(bukkitEntity);
        entity.damage(event.getDamage());

        // cancel the event
        event.setCancelled(true);
    }
}
