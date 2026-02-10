package net.mctitan.rpg.data.cooldown;

import net.mctitan.data.UUID;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.data.potion.PotionInfo;
import net.mctitan.rpg.enums.CraftableGroup;
import net.mctitan.rpg.enums.SpellActivation;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.CooldownModifier;
import net.mctitan.rpg.modifier.types.SpellModifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public class Cooldown extends Modable {
    private transient Entity entity;
    private transient int recoveryflat = 0;
    private transient double cooldownrecovery = 1;

    private transient HashMap<UUID, PotionCooldown> potioncooldowns = new HashMap<>();
    private TreeSet<PotionCooldown> potioncooldowncheck = new TreeSet<>();
    private HashMap<SpellType, Integer> spellcooldowns = new HashMap<>();

    public void entity(Entity entity) {
        // set entity
        this.entity = entity;

        // recreate cooldown map
        for(PotionCooldown potioncooldown : potioncooldowncheck) {
            potioncooldowns.put(potioncooldown.uuid(), potioncooldown);
        }
    }

    public int get(ItemStack stack) {
        int cooldown = 0;

        // check for potion
        if(stack.craftable().group() == CraftableGroup.POTION ||
                stack.craftable().group() == CraftableGroup.SPLASH_POTION ||
                stack.craftable().group() == CraftableGroup.LINGERING_POTION) {
            cooldown = get(new UUID(stack.uuid()));
        }

        // check for wand
        else if(stack.craftable().group() == CraftableGroup.WAND) {
            for (Modifier modifier : stack.modifiers()) {
                if (modifier instanceof SpellModifier spellmod && spellmod.activation() == SpellActivation.CAST) {
                    int spellcooldown = get(spellmod.spelltype());
                    if (spellcooldown > cooldown) {
                        cooldown = spellcooldown;
                    }
                }
            }
        }

        return cooldown;
    }

    public int get(UUID potionuuid) {
        // go through and remove cooldowns that are outdated
        cleanuppotions();

        // make sure potion cooldown exists
        if(!potioncooldowns.containsKey(potionuuid)) {
            return 0;
        }

        // return cooldown
        return potioncooldowns.get(potionuuid).cooldown() - entity.bukkitentity().getTicksLived();
    }

    public void set(java.util.UUID uuid, PotionInfo potioninfo) { set(new UUID(uuid), potioninfo); }
    public void set(UUID potionuuid, PotionInfo potioninfo) {
        // go through and remove cooldowns that are outdated
        cleanuppotions();

        // get some info about cooldown
        int base = potioninfo.cooldown();
        double recovery = potioninfo.cooldownrecovery();

        // get tick count
        int cooldown = entity.bukkitentity().getTicksLived() + cooldown(base, recovery);

        // set the cooldown
        PotionCooldown potioncooldown = new PotionCooldown(potionuuid, cooldown);
        potioncooldowns.put(potionuuid, potioncooldown);
        potioncooldowncheck.add(potioncooldown);
    }

    public void cleanuppotions() {
        Iterator<PotionCooldown> iter = potioncooldowncheck.iterator();
        while(iter.hasNext()) {
            PotionCooldown potioncooldown = iter.next();
            if(entity.bukkitentity().getTicksLived() >= potioncooldown.cooldown()) {
                iter.remove();
                potioncooldowns.remove(potioncooldown.uuid());
            } else {
                // set is sorted by cooldown, remaining elements are still good
                break;
            }
        }
    }

    public int get(SpellType spelltype) {
        if(!spellcooldowns.containsKey(spelltype)) {
            return 0;
        }

        if(entity.bukkitentity().getTicksLived() >= spellcooldowns.get(spelltype)) {
            spellcooldowns.remove(spelltype);
            return 0;
        }

        return spellcooldowns.get(spelltype) - entity.bukkitentity().getTicksLived();
    }

    public void set(SpellType spelltype, int base) {
        // get tick lived for cooldown after modifiers
        int cooldown = entity.bukkitentity().getTicksLived() + cooldown(base);

        // check to see if existing cooldown is longer than this one
        if(spellcooldowns.containsKey(spelltype) && spellcooldowns.get(spelltype) >= cooldown) {
            return;
        }

        // set the cool down
        spellcooldowns.put(spelltype, cooldown);
    }

    private int cooldown(int base) { return cooldown(base, 0); }
    private int cooldown(int base, double extrarecovery) {
        return (int)Math.ceil((base + recoveryflat) / (cooldownrecovery + extrarecovery));
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof CooldownModifier cooldownmmod) {
            switch(cooldownmmod.operator()) {
                case FLAT -> { recoveryflat += (int)cooldownmmod.value(); }
                case SCALER -> { cooldownrecovery += cooldownmmod.value() / 100; }
            }
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof CooldownModifier cooldownmmod) {
            switch(cooldownmmod.operator()) {
                case FLAT -> { recoveryflat -= (int)cooldownmmod.value(); }
                case SCALER -> { cooldownrecovery -= cooldownmmod.value() / 100; }
            }
        }
    }
}
