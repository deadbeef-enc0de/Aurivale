package net.mctitan.rpg.commands;

import net.mctitan.commands.BaseCommand;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Player;

public abstract class AurivaleBaseCommand extends BaseCommand {
    public AurivaleBaseCommand(String name) {
        super(name);
    }

    // baseline runCommand that is overridden
    @Override
    public boolean runCommand(org.bukkit.entity.Player bukkitplayer, String[] args) {
        Player player = DataManager.instance().player(bukkitplayer);
        return runCommand(player, args);
    }

    // this should never be called
    @Override
    public boolean runCommand(org.bukkit.entity.Player player) { return true; }

    // our internal run command with player and args
    // use this for downline commands that need the full argument list
    public boolean runCommand(Player player, String[] args) {
        return runCommand(player);
    }

    // our internal run command with just a player
    // use this for downline commands that use just the argument objects
    public boolean runCommand(Player player) {
        return this.help.runCommand(player.bukkitplayer());
    }
}
