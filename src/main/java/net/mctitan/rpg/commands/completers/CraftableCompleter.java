package net.mctitan.rpg.commands.completers;

import net.mctitan.commands.TabCompleter;
import net.mctitan.commands.data.Trie;
import net.mctitan.rpg.crafting.Craftables;

import java.util.List;

public class CraftableCompleter implements TabCompleter {
    private Trie trie = new Trie();

    public CraftableCompleter() {
        for(String craftablename : Craftables.instance().names()) {
            trie.add(craftablename);
        }
    }

    public List<String> getPossibleEntries(String argument) {
        return trie.match(argument);
    }
}
