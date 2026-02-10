package net.mctitan.rpg.data.message;

import net.kyori.adventure.text.Component;
import net.mctitan.data.BasicData;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.MessageType;

public abstract class Message extends BasicData {
    /** @return type of message */
    public abstract MessageType type();

    /** @return message string */
    public abstract Component message();

    /**
     * Ticks the message, the tick rate is 1/second
     * @return true if the message is still valid, false if it should be removed
     */
    public abstract boolean tick();

    /**
     * Called when the message is removed for a call back to change player data
     * Default behavior is to do nothing
     * @param player which player the message was removed from
     */
    public void removed(Player player) {}
}
