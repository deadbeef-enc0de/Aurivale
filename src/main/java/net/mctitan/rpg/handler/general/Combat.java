package net.mctitan.rpg.handler.general;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import net.mctitan.data.UUID;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.action.attack.AttackAction;
import net.mctitan.rpg.data.action.attack.AttackInstance;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.enums.CancelType;
import net.mctitan.rpg.enums.CraftableGroup;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.enums.SpellActivation;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.directional.Vector;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;
import java.util.logging.Level;

public class Combat implements Listener, Logger {
    private static final double SPLASH_ATTACK_MULTI = 0.719104;

    private static final Combat instance = new Combat();
    private Combat() {}
    public static Combat instance() { return instance; }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if(event.getReason() != EntityTargetEvent.TargetReason.CLOSEST_PLAYER) {
            return;
        }

        Player player = DataManager.instance().player((org.bukkit.entity.Player) event.getTarget());
        event.setCancelled(player.aggroprotect());
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        // get the projectile
        Projectile projectile = event.getEntity();

        // make sure the shooter is a living entity
        if(!(projectile.getShooter() instanceof LivingEntity bukkitentity)) {
            return;
        }

        // make sure the shooter doesn't have this projectile already
        Entity shooter = DataManager.instance().entity(bukkitentity);
        if(shooter.projectiles().has(projectile.getUniqueId())) {
            return;
        }

        // get the attack instance
        AttackAction action = shooter.actions().attack(shooter, projectile);
        AttackInstance attackinstance = action.instance(shooter);
        attackinstance.attackmultiplier(1);

        // add the projectile
        shooter.projectiles().add(projectile.getUniqueId(), attackinstance);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        // if we didn't shoot a projectile, do nothing
        if(!(event.getProjectile() instanceof Projectile)) {
            return;
        }

        // get some details
        Projectile projectile = (Projectile) event.getProjectile();
        Entity shooter = DataManager.instance().entity(event.getEntity());
        ItemStack bow = new ItemStack(shooter.bukkitentity().getEquipment().getItemInMainHand());
        AttackAction action = shooter.actions().attack(shooter, projectile);
        AttackInstance attackinstance = null;

        // if projectile is an arrow add modifiers and don't let it be picked up
        if(projectile.getType() == EntityType.ARROW) {
            // get stack and add modifiers
            ItemStack consumable = new ItemStack(event.getConsumable());
            attackinstance = action.instance(shooter, consumable.modifiers());

            // convert to arrow object and disallow pickups
            AbstractArrow arrow = (AbstractArrow) projectile;
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        } else {
            attackinstance = action.instance(shooter);
        }

        // get the projectile force and set as attack multiplier
        double force = event.getForce();
        log(Level.INFO, String.format("%s projectile=%s force=%.02f", shooter.name(), projectile.getType(), force));
        attackinstance.attackmultiplier(force * force);

        // set projectile velocity
        log(Level.INFO, String.format("Projectile speed=%.02fx attackmulti=%.02f", attackinstance.projectilespeed(), attackinstance.attackmultiplier()));
        projectile.setVelocity(projectile.getVelocity().multiply(attackinstance.projectilespeed()));

        // add projectile to player
        shooter.projectiles().add(projectile.getUniqueId(), attackinstance);
    }

    @EventHandler
    public void onProjectileHitBlock(ProjectileHitEvent event) {
        if(event.getHitBlock() == null || event.getEntity().getType() != EntityType.ARROW ||
           !(event.getEntity().getShooter() instanceof LivingEntity livingentity)) {
            return;
        }

        // get entity that fired projectile
        UUID projectile = new UUID(event.getEntity().getUniqueId());
        Entity entity = DataManager.instance().entity(livingentity);
        if(entity == null) {
            return;
        }

        // remove projectile
        log(Level.INFO, String.format("Arrow fired by %s hit block, removing", entity.name()));
        entity.projectiles().remove(projectile);
    }

    @EventHandler
    public void onAttackReset(PlayerInteractEvent event) {
        // get player
        Player player = DataManager.instance().player(event.getPlayer());

        // check for reset
        if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // reset attack
            player.actions().attack().attacktime().reset(player);
        }
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if(!(event.getEntity() instanceof LivingEntity bukkitevent)) {
            return;
        }

        Entity entity = DataManager.instance().entity(bukkitevent);
        log(Level.INFO, String.format("%s primed for explosion", entity.name()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // if already cancelled do nothing
        if(event.isCancelled()) {
            return;
        }

        // setup some data that will be needed
        org.bukkit.entity.Entity rawdamager = event.getDamager();
        org.bukkit.entity.Entity rawdamagee = event.getEntity();
        org.bukkit.entity.Projectile projectile = null;
        AttackInstance attackinstance = null;
        boolean melee = true;

        // check to see if this was projectile damage
        if(rawdamager instanceof org.bukkit.entity.Projectile) {
            melee = false;
            projectile = (org.bukkit.entity.Projectile) rawdamager;
            if(projectile.getShooter() instanceof org.bukkit.entity.Entity) {
                rawdamager = (org.bukkit.entity.Entity) projectile.getShooter();
            }
        }

        // get evoker entity from the fangs attack
        if(rawdamager.getType() == EntityType.EVOKER_FANGS) {
            EvokerFangs evokerfangs = (EvokerFangs) rawdamager;
            rawdamager = evokerfangs.getOwner();
        }

        // get Aurivale entity objects, failure means doing nothing here
        if(!(rawdamager instanceof org.bukkit.entity.LivingEntity) ||
           !(rawdamagee instanceof org.bukkit.entity.LivingEntity)) {
            return;
        }
        Entity damager = DataManager.instance().entity((org.bukkit.entity.LivingEntity)rawdamager);
        Entity damagee = DataManager.instance().entity((org.bukkit.entity.LivingEntity)rawdamagee);

        // verify damager and damagee
        if(damager == null) { log(Level.SEVERE, String.format("damager is null, attack cancelled")); }
        else if(damagee == null) { log(Level.SEVERE, String.format("damagee is null, attack cancelled")); }
        if(damagee == null || damager == null) {
            event.setCancelled(true);
            return;
        }
        log(Level.INFO, String.format("%s attacking %s", damager.name(), damagee.name()));

        // get the attack instance
        synchronized (damager) {
            if (melee) {
                AttackAction action = damager.actions().attack(event);
                attackinstance = action.instance(damager);
            } else {
                // get attack instance
                attackinstance = damager.projectiles().get(projectile.getUniqueId());

                // remove projectile from shooter
                damager.projectiles().remove(projectile.getUniqueId());

                // remove projectile from world unless it is a trident
                if(projectile.getType() != EntityType.TRIDENT) {
                    projectile.remove();
                }
            }
        }

        // verify attack instance
        if(attackinstance == null) {
            log(Level.SEVERE, String.format("Missing attack instance %s -> %s melee=%s", damager.name(), damagee.name(), melee));
            event.setCancelled(true);
            return;
        }

        // call on hit and when hit spells
        damagee.actions().spell().activate(SpellActivation.WHEN_HIT, damager);
        if(melee && attackinstance.attackmultiplier() > 0.8) {
            damager.actions().spell().activate(SpellActivation.ON_MELEE_HIT, damagee);
        }

        // get the damage roll for this damage event
        DamageRoll damageroll = attackinstance.roll();
        log(Level.INFO, String.format("Damage Roll damage=%.01f attackmulti=%.02f", damageroll.damage(), attackinstance.attackmultiplier()));

        // We have to manually handle sweep damage, also check for splash damage here
        if(attackinstance.hassplash() &&
                attackinstance.attackmultiplier() > SPLASH_ATTACK_MULTI &&
                event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            // get center of damagee
            Location damageecenter = damagee.bukkitentity().getBoundingBox().getCenter().toLocation(damagee.bukkitentity().getWorld());

            // get all entities nearby the damagee center
            for(LivingEntity splashentity : damageecenter.getNearbyLivingEntities(2.5, 1, 2.5)) {
                // skip attacker and defender
                if(splashentity == damager.bukkitentity() || splashentity == damagee.bukkitentity()) {
                    continue;
                }

                // make sure the found entity is actually close enough
                Location splashcenter = splashentity.getBoundingBox().getCenter().toLocation(splashentity.getWorld());
                Location attackercenter = damager.bukkitentity().getBoundingBox().getCenter().toLocation(damager.bukkitentity().getWorld());
                if(projectile != null) {
                    attackercenter = projectile.getBoundingBox().getCenter().toLocation(projectile.getWorld());
                }
                if(damageecenter.distance(splashcenter) > 2 || attackercenter.distance(splashcenter) > 5) {
                    continue;
                }

                // emit new EntityDamageEntityEvent with ENTITY_SWEEP_ATTACK
                EntityDamageByEntityEvent splashevent = new EntityDamageByEntityEvent(
                        projectile != null ? projectile : damager.bukkitentity(),
                        splashentity,
                        EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK,
                        DamageSource.builder(org.bukkit.damage.DamageType.GENERIC)
                                .withCausingEntity(projectile != null ? projectile : damager.bukkitentity())
                                .withDirectEntity(projectile != null ? projectile : damager.bukkitentity())
                                .withDamageLocation(splashentity.getBoundingBox().getCenter().toLocation(splashentity.getWorld()))
                                .build(),
                        new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, 0.0)),
                        new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(0.0))),
                        false
                );
                splashevent.callEvent();
            }
        }

        // apply splash damage when detected
        if(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            double splashmulti = attackinstance.splashdamage();
            log(Level.INFO, String.format("Splash damage multi=%.02f", splashmulti));
            damageroll.multiply(splashmulti);
        }

        // apply critical to damage roll
        log(Level.INFO, String.format("Checking for critical, chance=%.02f%%", attackinstance.critical().chance() * 100));
        boolean critical = attackinstance.critical().iscritical();
        if(critical) {
            log(Level.INFO, String.format("Attack is a critical, dealing %.01f%% more damage", attackinstance.critical().damage() * 100));
            damageroll.multiply(1 + attackinstance.critical().damage());
        } else {
            // apply non crit multiplier
            double multiplier = 1 - attackinstance.cancellation().value(CancelType.NON_CRIT);
            log(Level.INFO, String.format("Attack is not a critical, dealing %.02fx damage", multiplier));
            damageroll.multiply(multiplier);
        }

        // lock damagee for the following checks
        DamageRoll defenseroll;
        DamageRoll shieldroll;
        boolean blocked = false;
        synchronized (damagee) {
            // check for damage reflection and apply to damager
            DamageRoll reflected = damagee.defense().damagereflection().reflect(damageroll);
            reflected = damager.defense().apply(reflected);
            if(reflected.damage() > 0.01) {
                log(Level.INFO, String.format("%s reflected %.01f damage to %s", damagee.name(), reflected.damage(), damager.name()));
                damager.damage(reflected.damage());
            }

            // apply damagee defense stats
            defenseroll = damagee.defense().apply(damageroll);

            // apply damagee shield blocking
            shieldroll = defenseroll;
            if(damagee instanceof Player playerdamagee && playerdamagee.bukkitplayer().isBlocking() &&
               playerdamagee.canBlock(damager, projectile)) {
                // set blocked flag
                blocked = true;

                // block part of the damage
                log(Level.INFO, String.format("%s blocked %.01f%% damage", damagee.name(), 100*damagee.defense().blocking().blocked()));
                shieldroll = damagee.defense().blocking().apply(defenseroll);
            }
        }

        // apply healing from life leech
        double lifeleech = attackinstance.leech().lifeleech(shieldroll);
        if(lifeleech > 0) {
            log(Level.INFO, String.format("%s gaining %.01f life from leeching", damager.name(), lifeleech));
            damager.bukkitentity().heal(lifeleech);
        }

        // check for damage being above 0.01
        if(shieldroll.damage() < 0.01) {
            event.setCancelled(true);
            return;
        }

        // apply inflictions to damagee
        for(StatusEffect effect : attackinstance.inflictions().effects()) {
            damagee.statuseffects().addexternal(effect);
        }

        // lock damagee again for damage and knockback
        synchronized (damagee) {
            // damage damagee
            log(Level.INFO, String.format("Applying Defenses for %s", damagee.name()));
            log(Level.INFO, String.format("  Total Damage %.01f -> %.01f", damageroll.damage(), shieldroll.damage()));
            for (DamageType damagetype : shieldroll.damagetypes()) {
                log(Level.INFO, String.format("    %s Damage %.01f -> %.01f",
                        damagetype.string(), damageroll.damage(damagetype), shieldroll.damage(damagetype)));
            }

            // add last damage for player attackers
            if(damager instanceof Player player) {
                damagee.lastdamage().add(damagee, player, shieldroll.damage(), attackinstance.dropluck());
            }

            // add target for non-passive mobs
            if(!damagee.monster().passive() && damagee.mob() != null) {
                // send out target event
                EntityTargetEvent targetEvent = new  EntityTargetEvent(
                        damagee.bukkitentity(),
                        damager.bukkitentity(),
                        EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY
                );
                targetEvent.callEvent();

                // if target event is not cancelled, set target
                if(!targetEvent.isCancelled()) {
                    damagee.mob().setTarget(damager.bukkitentity());
                }
            }

            // damage entity
            damagee.damage(shieldroll.damage());

            // apply knock back if the attack multi is > 0.8
            if(attackinstance.attackmultiplier() > 0.8) {
                // Get knock back direction
                Vector direction;
                if(melee) {
                    Vector damageevector = new Vector(damagee.bukkitentity().getLocation());
                    Vector damagervector = new Vector(damager.bukkitentity().getLocation());
                    direction = damageevector.subtract(damagervector);
                } else {
                    direction = new Vector(projectile.getVelocity());
                }

                // get sprinting and blocking magnitude multipliers
                double sprinting = 1;
                if(melee && attackinstance.sprinting()) { sprinting = 1.5; }
                double blocking = blocked ? 0.2 : 1;

                // get knock back magnitude and height, normalize direction, then apply magnitude
                int knockback = attackinstance.knockback();
                double magnitude = 0.10 * (1.5 + 2.5 * knockback) * sprinting * blocking;
                double height = (0.25 + 0.065 * knockback) * blocking;
                direction = direction.y(0).normalize().multiply(magnitude).y(height);

                // fix non-finite issues with x/z directions
                if(!Double.isFinite(direction.x())) { direction = direction.x(0); }
                if(!Double.isFinite(direction.z())) { direction = direction.z(0); }

                // apply the knock back vector
                damagee.bukkitentity().setVelocity(direction.bukkit());
            }
        }

        // weapon only takes durability damage if it is a melee hit
        if(melee) {
            // weapon durability damage based on prevented damage
            int weapondurability = 1 + (int) Math.sqrt(damageroll.damage() - shieldroll.damage());
            ItemStack weapon = new ItemStack(damager.bukkitentity().getEquipment().getItemInMainHand());
            weapon.adddamage(weapondurability);
        }

        // get all armor pieces
        Set<CraftableGroup> armortypes = Set.of(CraftableGroup.HELMET,
                CraftableGroup.CHESTPLATE,
                CraftableGroup.LEGGINGS,
                CraftableGroup.BOOTS,
                CraftableGroup.SHIELD);
        List<ItemStack> armorpieces = new ArrayList<>();
        armorpieces.addAll(Arrays.stream(damagee.bukkitentity().getEquipment().getArmorContents()).map(ItemStack::new).toList());
        armorpieces.add(new ItemStack(damagee.bukkitentity().getEquipment().getItemInOffHand()));
        armorpieces.stream().filter(itemstack -> itemstack.craftable() != null)
                .filter(item -> armortypes.contains(item.craftable().group()))
                .toList();

        // armor durability damage is based on damage prevented but spread between all pieces
        int armordurability = 1 + (int) Math.sqrt((damageroll.damage() - shieldroll.damage()) / armorpieces.size());
        for(ItemStack armorpiece : armorpieces) {
            armorpiece.adddamage(armordurability);
        }

        // cancel original event since we handle everything
        event.setCancelled(true);
    }
}
