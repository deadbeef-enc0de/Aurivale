package net.mctitan.rpg.commands.completers;

import net.mctitan.commands.TabCompleter;
import net.mctitan.commands.data.Trie;
import net.mctitan.rpg.unique.Uniques;

import java.util.List;

public class UniqueCompleter implements TabCompleter {
    private Trie trie = new Trie();

    public UniqueCompleter() {
        for(String unique : Uniques.instance().uniqueids()) {
            trie.add(unique);
        }
    }

    public List<String> getPossibleEntries(String argument) {
        return trie.match(argument);
    }
}
