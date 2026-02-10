package net.mctitan.rpg.data;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.mctitan.data.BaseData;
import net.mctitan.data.UUID;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.action.Actions;
import net.mctitan.rpg.data.cooldown.Cooldown;
import net.mctitan.rpg.data.damage.Environmental;
import net.mctitan.rpg.data.damage.LastDamage;
import net.mctitan.rpg.data.defense.Defense;
import net.mctitan.rpg.data.enchantment.Enchantments;
import net.mctitan.rpg.data.luck.DropLuck;
import net.mctitan.rpg.data.pack.EntityPack;
import net.mctitan.rpg.data.projectile.Projectiles;
import net.mctitan.rpg.data.random.Randoms;
import net.mctitan.rpg.data.status.StatusEffects;
import net.mctitan.rpg.enums.RandomType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.AttributeModifier;
import net.mctitan.rpg.monster.Monster;
import net.mctitan.rpg.monster.Monsters;
import net.mctitan.rpg.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

import java.util.*;
import java.util.logging.Level;

public class Entity extends BaseData implements Logger {
    private HashSet<Modifier> modifiers = new HashSet<>();

    private transient final Actions actions = new Actions();
    private transient final Defense defense = new Defense();
    private transient final DropLuck dropluck = new DropLuck();
    private transient final Enchantments enchantments = new Enchantments();
    private transient final Equipment equipment = new Equipment();

    private String name;
    private String monster;
    private UUID pack;
    protected double health;
    private boolean noloot = false;
    private Environmental environment = new Environmental();
    private LastDamage lastdamage = new LastDamage();
    private Projectiles projectiles = new Projectiles();
    private StatusEffects statuseffects = new StatusEffects();
    private Cooldown cooldown = new Cooldown();
    private Randoms randoms = new Randoms();

    private transient LivingEntity bukkitentity;
    private transient boolean regouping = false;

    public Entity() {} // for YamlSaver

    public Entity(LivingEntity entity) {
        this(entity.getUniqueId());
        this.bukkitentity = entity;
        this.name = bukkitentity().getName();

        // make sure bukkit entity can't naturally despawn
        bukkitentity().setPersistent(true);

        // get and set monster modifiers
        Monster monster = Monsters.instance().get(bukkitentity().getType());
        this.monster = monster.name();
        monster.ctor(this);

        // set current health to max health 2 ticks later
        bukkitentity().getScheduler().runDelayed(Aurivale.instance(), task -> {
            if(bukkitentity().getHealth() > 0) {
                // need to catch if the entity was created because of combat
                // this should mean it has no increased health modifiers
                if(lastdamage.mostrecent() != null) {
                    health = bukkitentity().getHealth();
                } else {
                    health = maxhealth();
                }
                log(Level.INFO, String.format("Set %s health=%.02f", name, health));
            }
        }, null, 2);
    }

    private Entity(java.util.UUID uuid) { this(new UUID(uuid)); }
    private Entity(UUID uuid) { super(uuid); }

    public LivingEntity bukkitentity() {
        LivingEntity entity = (LivingEntity)Bukkit.getEntity(this.uuid.uuid());
        if(entity == null) { entity = this.bukkitentity; }
        else if(this.bukkitentity != entity) { this.bukkitentity = entity; }
        return entity;
    }

    public Mob mob() {
        LivingEntity bukkitentity = bukkitentity();
        if(!(bukkitentity instanceof Mob)) {
            return null;
        }
        return (Mob)bukkitentity;
    }

    public void initialize() {
        log(Level.INFO, String.format("Entity::initialize() for %s", name()));

        // damage ticks
        log(Level.INFO, String.format("Setting no damage immunity for %s", name()));
        bukkitentity().setNoDamageTicks(0);
        bukkitentity().setMaximumNoDamageTicks(0);

        // setup cooldown system
        cooldown.entity(this);

        // setup objects that need access to this object
        log(Level.INFO, String.format("Setting status effects for %s", name()));
        statuseffects.entity(this);

        // add monster actions
        actions.entity(this);
        Monster monster = Monsters.instance().get(this.monster);
        monster.apply(this);

        // modifiers
        log(Level.INFO, String.format("Applying modifiers to %s", name()));
        for(Modifier modifier : modifiers) {
            log(Level.INFO, String.format("  Applying to %s Modifier %s", name(), PlainTextComponentSerializer.plainText().serialize(modifier.text())));
            modifier.apply(this);
        }

        // set health of entity in 0.15s (to wait for equipment)
        // TODO get this to run right now
        bukkitentity().getScheduler().runDelayed(Aurivale.instance(), task -> {
            // make sure entity health is not higher than it's max health
            double maxhealth = maxhealth();
            if(health > maxhealth) {
                health = maxhealth;
            } else if(health <= 0) {
                health = bukkitentity().getHealth();
            }

            // reset entity health
            bukkitentity().setHealth(health);
        }, null, 3);
    }

    public void disable() {
        log(Level.INFO, String.format("Entity::disable() for %s", name()));

        // unapply modifiers
        log(Level.INFO, String.format("Unapplying modifiers to %s", name()));
        for(Modifier modifier : modifiers) {
            log(Level.INFO, String.format("  Unapplying to %s Modifier %s", name(), PlainTextComponentSerializer.plainText().serialize(modifier.text())));
            modifier.unapply(this);
        }
    }

    public void addattribute(AttributeModifier modifier) {
        if(bukkitentity().getAttribute(modifier.attribute().attribute()) == null) { return; }
        bukkitentity().getAttribute(modifier.attribute().attribute()).addTransientModifier(modifier.attributemod());
    }

    public void removeattribute(AttributeModifier modifier) {
        if(bukkitentity().getAttribute(modifier.attribute().attribute()) == null) { return; }
        bukkitentity().getAttribute(modifier.attribute().attribute()).removeModifier(modifier.key());
    }

    public final void add(Modifier modifier) { modifiers.add(modifier); }

    public void apply(Modifier modifier) {
        log(Level.INFO, String.format("Applying to %s Modifier %s", name(), PlainTextComponentSerializer.plainText().serialize(modifier.text())));
        modifiers.add(modifier);
        modifier.apply(this);
    }

    public void damage(double damage) {
        // damage entity and get resulting health
        double oldhealth = health();
        bukkitentity().damage(damage);
        health = bukkitentity().getHealth();

        // get damage for particle use, this is capped by damage, health, or 20
        double particledamage = Math.min(Math.min(damage, oldhealth), 20d);

        // create heart particles
        World world = bukkitentity().getLocation().getWorld();
        int particles = (int)Math.max(1, Math.ceil(particledamage / 2));
        world.spawnParticle(Particle.DAMAGE_INDICATOR, bukkitentity().getEyeLocation().add(0, 0, 0), particles, 0.25, 0.1, 0.25, 0);
    }

    public void heal(double damage) {
        bukkitentity().heal(damage);
        health = bukkitentity().getHealth();
    }

    public Actions actions() { return actions; }
    public Defense defense() { return defense; }
    public DropLuck dropluck() { return dropluck; }
    public Enchantments enchantments() { return enchantments; }
    public Equipment equipment() { return equipment; }

    public String name() { return name; }
    public Monster monster() { return Monsters.instance().get(monster); }
    public EntityPack pack() { return (pack != null ? DataManager.instance().pack(pack) : null); }
    public double health() { return health; }
    public double maxhealth() { return bukkitentity().getAttribute(Attribute.MAX_HEALTH).getValue(); }
    public boolean noloot() { return noloot; }
    public Environmental environment() { return environment; }
    public LastDamage lastdamage() { return lastdamage; }
    public Projectiles projectiles() { return projectiles; }
    public StatusEffects statuseffects() { return statuseffects; }
    public Cooldown cooldown() { return cooldown; }
    public Randoms randoms() { return randoms; }
    public boolean regouping() { return regouping; }
    public Random random(RandomType type) { return randoms.random(type).random(); }

    public void pack(EntityPack pack) { this.pack = (pack != null ? pack.uuid : null); }
    public void health(double health) { this.health = health; }
    public void noloot(boolean noloot) { this.noloot = noloot; }
    public void regrouping(boolean regouping) { this.regouping = regouping; }
}
