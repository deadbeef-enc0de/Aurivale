package net.mctitan.rpg.handler.general;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.enums.EffectType;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.Text;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.potion.PotionEffect;

import java.util.logging.Level;

public class StatusEffectChanges implements Listener, Logger {
    private static final StatusEffectChanges instance = new StatusEffectChanges();

    private StatusEffectChanges() {}

    public static StatusEffectChanges instance() { return instance; }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        // if the entity is not living, do nothing and cancel
        if(!(event.getEntity() instanceof LivingEntity bukkitentity)) {
            event.setCancelled(true);
            return;
        }

        // if the cause is the turtle helmet, cancel event and bail
        if(event.getCause() == Cause.TURTLE_HELMET) {
            event.setCancelled(true);
            return;
        }

        // if a witch did this because of attack cancel it
        if(event.getEntityType() == EntityType.WITCH && event.getCause() == Cause.ATTACK) {
            event.setCancelled(true);
            return;
        }

        // if plugin cause, do nothing and let it happen
        if(event.getCause() == Cause.PLUGIN) {
            return;
        }

        Entity entity = DataManager.instance().entity(bukkitentity);
        EffectType effecttype = EffectType.effecttype(event.getModifiedType());

        if(effecttype == null) {
            log(Level.SEVERE, String.format("EffectType null from %s", event.getModifiedType()));
            event.setCancelled(true);
            return;
        }

        switch(event.getAction()) {
            // when a status effect is added
            case ADDED, CHANGED -> {
                PotionEffect effect = event.getNewEffect();
                if(effect.getDuration() == PotionEffect.INFINITE_DURATION) {
                    log(Level.INFO, String.format("Adding untracked effect %s %s because %s %s",
                            effecttype, Text.romanint(effect.getAmplifier() + 1), event.getAction(), event.getCause()
                    ));
                } else {
                    double time = effect.getDuration() / 20d;
                    log(Level.INFO, String.format("Adding untracked effect %s %s for %.02fs because %s %s",
                            effecttype, Text.romanint(effect.getAmplifier() + 1), time, event.getAction(), event.getCause()
                    ));
                }

                StatusEffect statuseffect = new StatusEffect(effect);
                entity.statuseffects().addexternal(statuseffect);
                event.setCancelled(true);
            }

            // when a status effect is removed
            case REMOVED -> {
                log(Level.INFO, String.format("Removing effect %s because %s", effecttype, event.getAction()));
                entity.statuseffects().finished(effecttype);
                event.setCancelled(true);
            }

            // when all status effects are cleared
            case CLEARED -> {
                log(Level.INFO, String.format("Removing effect=%s because %s", effecttype, event.getAction()));
                entity.statuseffects().cleared(effecttype);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = DataManager.instance().entity(event.getEntity());
        if(entity == null) {
            return;
        }

        entity.statuseffects().removeall();
    }
}
