package net.mctitan.rpg.gamerules;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class Gamerules implements Logger {
    private static final Gamerules instance = new Gamerules();
    private final Map<GameRule, Object> gamerules = new HashMap<>();

    private Gamerules() {}

    public static Gamerules instance() { return instance; }
    public static void initialize() { instance.load(); }

    private void load() {
        // load gamerules from config
        Configuration gamerulecsonfig = Aurivale.instance().getConfig("gamerules");

        // go through all game rules that exist
        for(Field gamerulefield : GameRules.class.getFields()) {
            try {
                GameRule<?> gamerule = (GameRule<?>) gamerulefield.get(null);
                Object obj = gamerulecsonfig.getObject(gamerule.getKey().getKey(), gamerule.getType());
                if(obj == null) { continue; }

                log(Level.INFO, String.format("Gamerule %s=%s", gamerule.getKey().getKey(), obj.toString()));
                gamerules.put(gamerule, obj);
            } catch (Exception e) {
                // do nothing, error means it isn't a gamerule
            }
        }

        // set gamerules on existing worlds a bit after server start
        Bukkit.getGlobalRegionScheduler().runDelayed(Aurivale.instance(), task -> {
            for(World world : Bukkit.getWorlds()) {
                apply(world);
            }
        }, 5);
    }

    public void apply(World world) {
        log(Level.INFO, String.format("Applying gamerules to world=%s", world.getName()));
        for(Map.Entry<GameRule, Object> entry : gamerules.entrySet()) {
            world.setGameRule(entry.getKey(), entry.getValue());
        }
    }
}
