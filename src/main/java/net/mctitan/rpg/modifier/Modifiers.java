package net.mctitan.rpg.modifier;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.ConfigurationSection;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;

public class Modifiers implements Logger {
    private static final Modifiers instance = new Modifiers();
    private Map<String, ModifierConfig> configs = new HashMap<>();
    private Map<String, Class<? extends Modifier>> classes = new HashMap<>();

    private Modifiers() {}

    public static Modifiers instance() { return instance; }
    public static void initialize() { instance.load(); }

    private void load() {
        // go through modifiers section
        ConfigurationSection modifiers = Aurivale.instance().getConfig("modifiers");
        for(String id : modifiers.getKeys(false)) {
            log(Level.INFO, "Loading modifier="+id);
            ConfigurationSection section = modifiers.getConfigurationSection(id);
            String classname = String.format("net.mctitan.rpg.modifier.configs.%s", section.getString("type"));
            try {
                Class<?> clazz = Class.forName(classname);
                Constructor ctor = clazz.getConstructor(String.class);
                ModifierConfig config = (ModifierConfig)ctor.newInstance(id);
                register(config);
            } catch(Exception e) {
                log(Level.WARNING, String.format("%s: %s",e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }

    public void register(String id, Class<? extends Modifier> clazz) { classes.put(id, clazz); }
    public void register(ModifierConfig config) {
        configs.put(config.id(), config);
        register(config.id(), config.modifierclass());
    }

    public ArrayList<String> configs() { return new ArrayList<>(configs.keySet()); }
    public ModifierConfig config(String id) { return configs.get(id); }
}
