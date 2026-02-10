package net.mctitan.rpg.spell;

import net.mctitan.data.BaseData;
import net.mctitan.data.UUID;
import net.mctitan.data.saver.PluginSaver;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.action.spell.SpellActionData;
import net.mctitan.rpg.enums.SpellActivation;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.handler.general.ItemCooldown;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.spell.projectile.SpellProjectile;
import net.mctitan.rpg.spell.type.Spell;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Level;

public class Spells implements Logger {
    private static Spells instance = new Spells();

    private PluginSaver data;
    private BitSet ids =  new BitSet(0x10000);
    private Map<UUID, SpellInstance> instances = new HashMap<>();
    private Map<UUID, SpellProjectile> projectiles = new HashMap<>();
    private Map<Location, SpellChangedBlock> blocks = new HashMap<>();

    private Spells() {
        // get data saver
        data = PluginSaver.saver(Aurivale.instance());
    }

    public static Spells instance() { return instance; }
    public static void initialize() { instance.load(); }

    public void load() {
        // @TODO add recipe for void walk spell

        // load spell instances
        for(SpellType spelltype : SpellType.values()) {
            // make sure the spell type is complete
            if(spelltype.spell() == null || spelltype.instanceclass() == null) {
                log(Level.WARNING, String.format("spelltype=%s incomplete spell=%s instance=%s",
                        spelltype,
                        spelltype.spell(),
                        spelltype.instanceclass()));
                continue;
            }
            log(Level.INFO, String.format("Loading spelltype=%s", spelltype));

            // initialize spell
            spelltype.spell().initialize();

            // load all spell instance data
            data.loadall(spelltype.instanceclass());

            // handle the spell instances of this type
            for(BaseData bd : data.get(spelltype.instanceclass())) {
                SpellInstance instance = (SpellInstance)bd;
                ids.set(instance.id());
                instances.put(instance.uuid, instance);
                instance.initialize();
            }
        }

        // load spell blocks
        data.loadall(SpellChangedBlock.class);
        for(BaseData bd : data.get(SpellChangedBlock.class)) {
            SpellChangedBlock block = (SpellChangedBlock)bd;
            blocks.put(block.location(), block);
        }

        // setup repeating task to handle spells covering each game tick
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(Aurivale.instance(), task -> { tick(); }, 1, 1);
    }

    public void disable() {
        for(SpellInstance instance : instances.values()) {
            data.save(instance);
        }

        for(SpellChangedBlock block : blocks.values()) {
            data.save(block);
        }
    }

    public SpellProjectile projectile(java.util.UUID uuid) { return projectile(new UUID(uuid)); }
    public SpellProjectile projectile(UUID uuid) { synchronized (projectiles) { return projectiles.get(uuid); } }

    public void register(SpellProjectile projectile) { synchronized (projectiles) { projectiles.put(projectile.uuid(), projectile); } }
    public void unregister(SpellProjectile projectile) { synchronized (projectiles) { projectiles.remove(projectile.uuid()); } }

    public SpellChangedBlock block(org.bukkit.Location location) { return block(new Location(location)); }
    public SpellChangedBlock block(Location location) { synchronized (blocks) { return blocks.get(location.blockloc()); } }
    public void register(SpellChangedBlock block) {
        synchronized (blocks) { blocks.put(block.location(), block); }
    }

    public void unregister(SpellChangedBlock block) {
        synchronized (blocks) { blocks.remove(block.location()); }
        synchronized (data) { data.remove(block); }
    }

    public int mana(Entity caster, List<SpellActionData> spelldata) {
        int mana = 0;
        for (SpellActionData data : spelldata) {
            // need to get adjusted level of spell and then mana
            int level = caster.actions().spell().level().value(data.level);
            int spellmana = data.spelltype.spell().manacost(level);
            mana += spellmana;
        }

        return mana;
    }

    public void cast(Entity caster, ItemStack stack) {
        // get all spells in the cating activation
        List<SpellActionData> spelldata = caster.actions().spell().spells(SpellActivation.CAST);

        // make sure there is no pending cooldown for the spells being cast
        for(SpellActionData data : spelldata) {
            if(caster.cooldown().get(data.spelltype) > 0) {
                return;
            }
        }

        // if caster is player, check mana
        if(caster instanceof Player player) {
            // get mana cost of all spells
            int needed = mana(caster, spelldata);

            // make sure player has enough mana to cast all spells
            if (player.mana().value() < needed) {
                return;
            }
        }

        // cast the spells
        for(SpellActionData data : spelldata) {
            // cast spell
            cast(caster, data.spelltype.spell(), data.level);
        }

        // tell player about the cooldown on the spells cast
        if(stack != null && caster instanceof Player player) {
            ItemCooldown.instance().setcoolddown(player, stack);
        }
    }

    public void cast(Entity caster, Spell spell, int level) { run(caster, null, spell, level, true); }
    public void activate(Entity caster, Spell spell, int level) { run(caster, null, spell, level, false); }
    public void activate(Entity caster, Entity target, Spell spell, int level) { run(caster, target, spell, level, false); }
    private void run(Entity caster, Entity target, Spell spell, int level, boolean cast) {
        // get next instance id available
        int id = ids.nextClearBit(0);
        ids.set(id);

        // apply level changes to spells
        level = caster.actions().spell().level().value(level);

        // cast or activate the spell
        SpellInstance instance = spell.instance(id, cast, level, caster, target);
        if(instance == null) {
            ids.clear(id);
            return;
        }

        // add cooldown for cast spells
        if(cast) {
            caster.cooldown().set(instance.type(), spell.cooldown(level));
        }

        // check to see if the instance is a duration spell
        if(!instance.instant()) {
            synchronized (instances) { instances.put(instance.uuid, instance); }
        }

        // start the spell, happens 1 tick later in the region of the caster
        instance.start();
    }

    public void tick() {
        // iterate over all spell instances
        Iterator<Map.Entry<UUID, SpellInstance>> iter = instances.entrySet().iterator();
        while(iter.hasNext()) {
            // get the instance
            Map.Entry<UUID, SpellInstance> entry = iter.next();
            SpellInstance instance = entry.getValue();

            // if the spell hasn't started yet or the caster is not online
            if(!instance.started() || instance.bukkitcaster() == null) {
                continue;
            }

            // run the tick on the instance
            instance.run();

            // check if the instance is done
            if(instance.done() || (instance.ticks() >= instance.duration())) {
                // remove the data file
                data.remove(instance);

                // remove the spell instance
                iter.remove();

                // end the spell instance
                instance.end();

                // reclaim the instance id
                ids.clear(instance.id());
            }
        }
    }
}
