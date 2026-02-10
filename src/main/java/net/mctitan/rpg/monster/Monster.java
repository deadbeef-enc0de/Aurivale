package net.mctitan.rpg.monster;

import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.crafting.Craftables;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.ItemSlot;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.Text;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.*;
import java.util.logging.Level;

public class Monster implements Logger {
    private String name;
    private boolean passive;
    private Map<String, List<ModifierTemplate>> modifiers = new HashMap<>();
    private Map<String, List<ModifierTemplate>> actionmodifiers = new HashMap<>();
    private Map<String, ConfigurationSection> actions = new HashMap<>();
    private List<Craftable> equipment = new LinkedList<>();

    public Monster(String name, ConfigurationSection section) {
        this.name = name;
        log(Level.INFO, String.format("  Loading monster %s", name));

        // load parent information
        if(section.contains("parent")) {
            List<String> parents = new ArrayList<>();
            if(section.isString("parent")) { parents.add(section.getString("parent")); }
            else { parents.addAll(section.getStringList("parent")); }

            for(String parentid : parents) {
                Monster parent = Monsters.instance().get(parentid);
                if(parent == null) {
                    log(Level.WARNING, String.format("    Could not find parent %s", parentid));
                    continue;
                }
                log(Level.INFO, String.format("    Loading parent %s", parentid));

                if(!passive && parent.passive()) { passive = true; }

                for(String id : parent.modifiers.keySet()) {
                    if(modifiers.containsKey(id)) { continue; }
                    modifiers.put(id, parent.modifiers.get(id));
                }

                for(String id : parent.actionmodifiers.keySet()) {
                    if(actionmodifiers.containsKey(id)) { continue; }
                    actionmodifiers.put(id, parent.actionmodifiers.get(id));
                }

                actions.putAll(parent.actions);
            }
        }

        // load passive flag if it exists
        if(section.contains("passive")) {
            passive = section.getBoolean("passive");
        }

        // load modifiers
        if(section.contains("modifiers")) {
            log(Level.INFO, "    Modifiers:");
            modifiers.put(name, new ArrayList<>());
            ConfigurationSection modifierssection = section.getConfigurationSection("modifiers");
            for(String modifierid :  modifierssection.getKeys(false)) {
                ConfigurationSection modifiersection = modifierssection.getConfigurationSection(modifierid);
                ModifierTemplate modifier = ModifierTemplate.template(modifiersection);
                if(modifier == null) {
                    log(Level.WARNING, String.format("      Could not load modifier %s", modifierid));
                    continue;
                }
                log(Level.INFO, String.format("      Loaded modifier %s", modifierid));
                modifiers.get(name).add(modifier);
            }
        }

        // load action modifiers
        if(section.contains("actionmodifiers")) {
            log(Level.INFO, "    Action Modifiers:");
            actionmodifiers.put(name, new ArrayList<>());
            ConfigurationSection actionmodifierssection = section.getConfigurationSection("actionmodifiers");
            for(String modifierid :  actionmodifierssection.getKeys(false)) {
                ConfigurationSection modifiersection = actionmodifierssection.getConfigurationSection(modifierid);
                ModifierTemplate modifier = ModifierTemplate.template(modifiersection);
                if(modifier == null) {
                    log(Level.WARNING, String.format("      Could not load action modifier %s", modifierid));
                    continue;
                }
                log(Level.INFO, String.format("      Loaded action modifier %s", modifierid));
                actionmodifiers.get(name).add(modifier);
            }
        }

        // load actions
        if(section.contains("actions")) {
            log(Level.INFO, "    Actions:");
            ConfigurationSection actionssection = section.getConfigurationSection("actions");
            for(String actionid : actionssection.getKeys(false)) {
                ConfigurationSection actionsection = actionssection.getConfigurationSection(actionid);
                log(Level.INFO, String.format("      Loaded action %s", actionid));
                actions.put(actionid, actionsection);
            }
        }

        // load any equipment
        if(section.contains("equipment") && section.isList("equipment")) {
            log(Level.INFO, "    Equipment:");
            for(Object object : section.getList("equipment")) {
                if(!(object instanceof Map map)) { continue; }
                MemoryConfiguration itemconfig = new MemoryConfiguration();
                itemconfig.addDefaults(map);

                String craftablename = itemconfig.getString("craftable");
                Craftable craftable = Craftables.instance().craftable(craftablename);
                if(craftable == null) {
                    log(Level.WARNING, String.format("Cannot give %s craftable %s, does not exist", name, craftablename));
                    continue;
                }
                log(Level.INFO, String.format("      Added equipment=%s", craftablename));
                equipment.add(craftable);
            }
        }
    }

    public String name() { return name; }
    public boolean passive() { return passive; }
    public boolean hasequipment() { return !equipment.isEmpty(); }
    public Map<String, ConfigurationSection> actions() { return new HashMap<>(actions); }

    public List<ModifierTemplate> modifiers() {
        ArrayList<ModifierTemplate> ret = new ArrayList<>();
        for(Map.Entry<String, List<ModifierTemplate>> entry : modifiers.entrySet()) {
            ret.addAll(entry.getValue());
        }
        return ret;
    }

    public List<ModifierTemplate> actionmodifiers() {
        ArrayList<ModifierTemplate> ret = new ArrayList<>();
        for(Map.Entry<String, List<ModifierTemplate>> entry : actionmodifiers.entrySet()) {
            ret.addAll(entry.getValue());
        }
        return ret;
    }

    public void ctor(Entity entity) {
        // add modifiers
        for(ModifierTemplate template : modifiers()) {
            entity.add(template.modifier());
        }

        // add equipment ot monster
        for(Craftable craftable : equipment) {
            // get item and set item
            ItemStack stack = craftable.create();
            entity.bukkitentity().getEquipment().setItem(craftable.itemslot().bukkit(), stack.bukkitstack());

            // set drop chance to always drop
            entity.bukkitentity().getEquipment().setDropChance(craftable.itemslot().bukkit(), Monsters.craftabledrops());
        }
    }

    public void apply(Entity entity) {
        // apply actions
        for(Map.Entry<String, ConfigurationSection> actionentry : actions().entrySet()) {
            entity.actions().addaction(actionentry.getKey(), actionentry.getValue());
        }

        // apply monster action modifiers
        for(ModifierTemplate template : actionmodifiers()) {
            entity.actions().apply(template.modifier());
        }

        // don't apply equipment to players, equipment change handler does this for us
        if(entity instanceof Player) {
            return;
        }

        // go through entity equipment, convert any to Aurivale stacks, then add modifiers
        for(ItemSlot slot : ItemSlot.values()) {
            if(slot.bukkit() == null) { continue; }

            // get raw item stack and verify it's worth keeping
            org.bukkit.inventory.ItemStack rawstack = entity.bukkitentity().getEquipment().getItem(slot.bukkit());
            if(rawstack.getType().isAir()) { continue; }

            // get item stack for raw item
            ItemStack stack = new ItemStack(rawstack);

            // if item is not a craftable make an equivalent craftable item stack
            if(stack.craftable() == null) {
                Craftable craftable = Craftables.instance().craftable(Text.enumtocraftable(stack.bukkitstack().getType().name()));
                if(craftable == null) {
                    // if there is no craftable move on
                    continue;
                }

                // get new item stack, set the item in slot, and set to always drop
                stack = craftable.create();
                entity.bukkitentity().getEquipment().setItem(slot.bukkit(), stack.bukkitstack());
                entity.bukkitentity().getEquipment().setDropChance(slot.bukkit(), Monsters.craftabledrops());
            }

            // add all modifiers to monster
            for(Modifier modifier : stack.modifiers()) {
                modifier.apply(entity);
            }
        }
    }
}
