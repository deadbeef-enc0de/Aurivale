package net.mctitan.rpg.config;

import net.mctitan.rpg.Aurivale;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class ConfigPart {
    private YamlConfiguration config;
    private final String name;

    public ConfigPart(String name) { this(name, true); }
    public ConfigPart(String name, boolean save) {
        this.name = name;
        this.config = file();
        if(this.config == null) {
            this.config = jar();
        } else {
            loadNewDefaults();
        }

        if(save) { save(); }
    }

    public YamlConfiguration config() { return config; }
    public String name() { return name; }

    public final void save() {
        File configdir = new File(String.format("%s%sconfig", Aurivale.instance().getDataFolder().getAbsolutePath(), File.separator));
        if(!configdir.exists()) {
            configdir.mkdir();
        }
        try {
            config.save(filepath());
        } catch (IOException ex) {}
    }

    private YamlConfiguration jar() {
        InputStream raw = Aurivale.instance().getResource(String.format("%s.yml",name));
        BufferedReader reader = new BufferedReader(new InputStreamReader(raw));
        return YamlConfiguration.loadConfiguration(reader);
    }

    private File filepath() {
        File data = Aurivale.instance().getDataFolder();
        String path = String.format("%s%s%s%s%s%s", data.getAbsolutePath(), File.separator, "config", File.separator, name, ".yml");
        return new File(path);
    }

    private YamlConfiguration file() {
        File filepath = filepath();
        if(filepath.exists()) {
            return YamlConfiguration.loadConfiguration(filepath);
        }
        return null;
    }

    private void loadNewDefaults() {
        YamlConfiguration defaults = jar();
        for(String key : defaults.getKeys(true)) {
            if(!this.config.contains(key)) {
                this.config.set(key, defaults.get(key));
            }
        }
    }
}
