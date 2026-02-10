package net.mctitan.rpg.pack;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.tables.tagged.TaggedTable;
import net.mctitan.rpg.data.tables.tagged.TaggedTableEntry;
import net.mctitan.rpg.data.tables.tagged.TaggedTableSelector;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Level;

public class PackModifiers implements Logger {
    private static final PackModifiers instance = new PackModifiers();

    private final Map<String, ModifierConfig> modifiers = new HashMap<>();
    private final TaggedTable<PackModifierGroup> groups = new TaggedTable<>();
    private ModifierConfig luck;

    private PackModifiers() {}

    public static PackModifiers instance() { return instance; }
    public static void initialize() { instance.load(); }

    private void load() {
        Configuration packconfig = Aurivale.instance().getConfig("packs");

        // load luck config
        luck = ModifierConfig.modifierconfig(packconfig.getConfigurationSection("modifier_luck_drop"));

        // load individual modifiers
        ConfigurationSection modifierssection = packconfig.getConfigurationSection("modifiers");
        for(String modifierid : modifierssection.getKeys(false)) {
            ConfigurationSection modifiersection = modifierssection.getConfigurationSection(modifierid);
            ModifierConfig modifier = ModifierConfig.modifierconfig(modifiersection);
            if(modifier != null) {
                log(Level.INFO, String.format("Loaded pack modifier=%s", modifier.id()));
                modifiers.put(modifierid, modifier);
            }
        }

        // load up modifier groups
        ConfigurationSection groupssection = packconfig.getConfigurationSection("modifier_groups");
        for(String groupid : groupssection.getKeys(false)) {
            ConfigurationSection groupsection = groupssection.getConfigurationSection(groupid);

            // get weight
            if(!groupsection.contains("weight")) {
                log(Level.WARNING, String.format("Cannot load group=%s, missing weight", groupid));
                continue;
            }
            int weight = groupsection.getInt("weight");

            PackModifierGroup modgroup = new PackModifierGroup(groupid, weight);
            log(Level.INFO, String.format("Loading pack modifier group=%s", groupid));

            // go through and add each modifier table
            for(String modifierid : groupsection.getStringList("modifiers")) {
                ModifierConfig modifier = modifiers.get(modifierid);
                if(modifier == null) {
                    log(Level.WARNING, String.format("  Could not add missing modifier=%s", modifierid));
                    continue;
                }

                modgroup.add(modifier);
                log(Level.INFO, String.format("  Added pack modifier=%s", modifierid));
            }

            // add group to table
            groups.insert(modgroup, weight, groupid, List.of());
        }
    }

    public List<String> modifiers() { return new ArrayList<>(modifiers.keySet()); }
    public ModifierConfig modifier(String id) { return modifiers.get(id); }

    public List<Modifier> modifiers(int groupcount, int modcount) {
        // get groups to select modifiers from
        TaggedTableSelector groupselector = new TaggedTableSelector();
        TaggedTable<ModifierTemplate> modifiertable = new TaggedTable<>();
        Set<String> idranks = new HashSet<>();
        for(int g = 0; g < groupcount; g++) {
            // get group selection
            PackModifierGroup group = this.groups.subset(groupselector).get();
            if(group == null) { continue; }

            // insert into modifier table
            for(TaggedTableEntry<ModifierTemplate> entry : group.modifiers()) {
                // make sure idrank is not already in the list
                String idrank = String.format("%s:%d", entry.element().id(), entry.element().rank());
                if(idranks.contains(idrank)) { continue; }
                idranks.add(idrank);

                modifiertable.insert(entry);
            }

            // update selector so the same group can't be picked again
            groupselector.blacklist(group.id());
        }

        // get modifiers from groups selected
        TaggedTableSelector modselector = new TaggedTableSelector();
        List<Modifier> retmods = new LinkedList<>();
        for(int m = 0; m < modcount; m++) {
            // get modifier
            Modifier modifier = modifiertable.subset(modselector).get().modifier();

            // add modifier to return list
            retmods.add(modifier);

            // add luck modifier of the same rank
            retmods.add(luck.template(modifier.rank()).modifier());

            // update selector so same modifier can't be selected
            modselector.blacklist(modifier.id());
        }

        return retmods;
    }
}
