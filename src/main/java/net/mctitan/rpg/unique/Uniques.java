package net.mctitan.rpg.unique;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.data.tables.weighted.WeightedTable;
import net.mctitan.rpg.enums.CraftableGroup;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

public class Uniques implements Logger {
    private static Uniques instance = new Uniques();
    private HashMap<String, Unique> uniques = new HashMap<>();
    private HashMap<Craftable, WeightedTable<Unique>> craftables = new HashMap<>();
    private HashMap<CraftableGroup, WeightedTable<Unique>> groups = new HashMap<>();
    private WeightedTable<Unique> table = new WeightedTable<>();

    private Uniques() {}

    public static Uniques instance() { return instance; }
    public static void initialize() { instance.load(); }

    private void load() {
        Configuration config = Aurivale.instance().getConfig("uniques");
        for(String uniqueid : config.getKeys(false)) {
            ConfigurationSection uniquesection = config.getConfigurationSection(uniqueid);
            Unique unique = new Unique(uniquesection);
            if(unique.craftable() == null) {
                log(Level.WARNING, String.format("Unique %s has invalid craftable=%s", uniqueid, uniquesection.getString("craftable")));
                continue;
            }
            if(unique.modeldata() == null) {
                log(Level.WARNING, String.format("Unique %s missing model data", uniqueid));
            }

            log(Level.INFO, String.format("Loaded Unique %s", uniqueid));
            Craftable craftable = unique.craftable();

            // add to unique list
            uniques.put(uniqueid, unique);

            // add to craftable -> unique table
            if(!craftables.containsKey(craftable)) { craftables.put(craftable, new WeightedTable<>()); }
            craftables.get(craftable).insert(unique, unique.weight());

            // add to craftabel group -> unique table
            if(!groups.containsKey(craftable.group())) { groups.put(craftable.group(), new WeightedTable<>()); }
            groups.get(craftable.group()).insert(unique, unique.weight());

            // add to overall unique table
            table.insert(unique, unique.weight());
        }
    }

    public Set<String> uniqueids() { return uniques.keySet(); }

    public boolean has(String id) { return uniques.containsKey(id); }
    public boolean has(Craftable craftable) { return craftables.containsKey(craftable); }
    public boolean has(CraftableGroup craftableGroup) { return groups.containsKey(craftableGroup); }

    public Unique get(String id) { return uniques.get(id); }
    public Unique get() { return get(0); }
    public Unique get(int extra) { return table.get(extra); }
    public WeightedTable<Unique> get(Craftable craftable) { return craftables.get(craftable).clone(); }
    public WeightedTable<Unique> get(CraftableGroup group) { return groups.get(group).clone(); }
}
