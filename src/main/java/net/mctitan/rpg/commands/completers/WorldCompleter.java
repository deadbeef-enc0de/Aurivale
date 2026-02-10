package net.mctitan.rpg.commands.completers;

import net.mctitan.commands.TabCompleter;
import net.mctitan.commands.data.Trie;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;

public class WorldCompleter implements TabCompleter {
    public List<String> getPossibleEntries(String argument) {
        Trie trie = new Trie();
        for(World world : Bukkit.getWorlds()) {
            trie.add(world.getName());
        }

        return trie.match(argument);
    }
}
