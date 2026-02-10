package net.mctitan.rpg.data.damage;

import net.mctitan.data.BasicData;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.handler.general.EnvironmentalDamage;
import net.mctitan.rpg.util.Logger;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.HashMap;
import java.util.logging.Level;

public class Environmental extends BasicData implements Logger {
    private HashMap<DamageCause, Long> lastdamages = new HashMap<>();

    public boolean damage(Entity entity, DamageCause cause, Damage damage) {
        // get the number of no tick damage for that damage type
        long nodamageticks = EnvironmentalDamage.instance().ticks(cause);

        // if there was a last damage, check if enough time has passed
        if(lastdamages.containsKey(cause)) {
            long lastdamage = lastdamages.get(cause);
            if(entity.bukkitentity().getTicksLived() <= (lastdamage + nodamageticks)) {
                return false;
            }
        }

        // damage the entity and update the last damage time
        DamageRoll roll = damage.roll();
        roll = entity.defense().apply(roll);
        double taken = roll.damage();
        if(taken > 0) {
            log(Level.INFO, String.format("Dealing %.01f %s damage to %s", taken, cause.name(), entity.name()));
            entity.damage(taken);
            lastdamages.put(cause, (long) entity.bukkitentity().getTicksLived());
        }

        return true;
    }
}
