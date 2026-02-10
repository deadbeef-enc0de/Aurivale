package net.mctitan.rpg.config;

import net.mctitan.rpg.Aurivale;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    private final Map<String, ConfigPart> configs = new HashMap<>();

    public Configuration() {
        if(!Aurivale.instance().getDataFolder().exists()) {
            Aurivale.instance().getDataFolder().mkdir();
        }

        add(new ConfigPart("brewing"));
        add(new ConfigPart("craftables"));
        add(new ConfigPart("debug"));
        add(new ConfigPart("dimensions"));
        add(new ConfigPart("environmental"));
        add(new ConfigPart("gamerules"));
        add(new ConfigPart("handbook"));
        add(new ConfigPart("loot"));
        add(new ConfigPart("modifiers"));
        add(new ConfigPart("monsters"));
        add(new ConfigPart("packs"));
        add(new ConfigPart("players"));
        add(new ConfigPart("plugin", false));
        add(new ConfigPart("spawn"));
        add(new ConfigPart("spells"));
        add(new ConfigPart("status"));
        add(new ConfigPart("trading"));
        add(new ConfigPart("uniques"));
    }

    private void add(ConfigPart part) {
        configs.put(part.name(), part);
    }

    public org.bukkit.configuration.Configuration config(String config) {
        ConfigPart part = configs.get(config);
        if(part != null) {
            return part.config();
        }
        return new YamlConfiguration();
    }
}
