package net.mctitan.rpg.handler.enchantment;

import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.Enchantment;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileRider implements Listener {
    private static final ProjectileRider instance = new ProjectileRider();

    private ProjectileRider() {}

    public static ProjectileRider instance() { return instance; }

    @EventHandler
    public void onShootRiderArrow(EntityShootBowEvent event) {
        // check to see if item in hand has projectile rider
        Entity shooter = DataManager.instance().entity(event.getEntity());
        if(shooter.enchantments().level(Enchantment.projectile_rider) <= 0) {
            return;
        }

        // if the shooter is riding something, remove it's relative velocity
        if(shooter.bukkitentity().getVehicle() != null && shooter.bukkitentity().getVehicle() instanceof Projectile) {
            Vector projectilevelocity = new Vector(event.getProjectile().getVelocity());
            Vector vehiclevelocity = new Vector(shooter.bukkitentity().getVehicle().getVelocity());
            projectilevelocity = projectilevelocity.subtract(vehiclevelocity);
            event.getProjectile().setVelocity(projectilevelocity.bukkit());
        }

        // add the shooter to the arrow's passenger list
        event.getProjectile().addPassenger(shooter.bukkitentity());
    }

    @EventHandler
    public void onRideEnd(ProjectileHitEvent event) {
        if(event.getHitBlock() == null) {
            return;
        }

        for(org.bukkit.entity.Entity entity : event.getEntity().getPassengers()) {
            event.getEntity().removePassenger(entity);
        }
    }
}
