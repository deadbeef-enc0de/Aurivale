package net.mctitan.rpg.util;

import java.util.logging.Level;

import net.mctitan.rpg.Aurivale;

public interface Logger {
    default void log(Level level, String message) {
        LOG(level, message);
    }
    default void log(Level level, String message, Throwable throwable) { LOG(level, message, throwable);}

    static void LOG(Level level, String message) { Aurivale.instance().getLogger().log(level, message); }
    static void LOG(Level level, String message, Throwable throwable) { Aurivale.instance().getLogger().log(level, message, throwable); }
}
