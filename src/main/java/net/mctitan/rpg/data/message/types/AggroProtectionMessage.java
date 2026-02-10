package net.mctitan.rpg.data.message.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.message.Message;
import net.mctitan.rpg.enums.MessageType;

public class AggroProtectionMessage extends Message {
    private int duration;

    public AggroProtectionMessage() {} // for YamlSaver

    public AggroProtectionMessage(int duration) {
        this.duration = duration;
    }

    @Override
    public MessageType type() { return MessageType.AGGRO_PROTECTION; }

    @Override
    public Component message() {
        NamedTextColor color;
        if(duration < 60) { color = NamedTextColor.RED; }
        else if(duration < 300) { color = NamedTextColor.GOLD; }
        else { color = NamedTextColor.GRAY; }
        return Component.text(String.format("Peaceful %d:%02d:%02d",
                duration / 3600,
                (duration / 60) % 60,
                duration % 60))
                .color(color);
    }

    @Override
    public boolean tick() {
        --duration;
        return duration > 0;
    }

    @Override
    public void removed(Player player) {
        player.removeaggroprotection();
    }
}
