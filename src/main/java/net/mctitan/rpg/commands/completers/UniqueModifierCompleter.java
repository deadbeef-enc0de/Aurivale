package net.mctitan.rpg.commands.completers;

import net.mctitan.commands.TabCompleter;
import net.mctitan.commands.data.Trie;
import net.mctitan.rpg.unique.Unique;
import net.mctitan.rpg.unique.Uniques;

import java.util.List;

public class UniqueModifierCompleter implements TabCompleter {
    private Unique unique = null;
    private Trie trie = null;

    public UniqueModifierCompleter() {}

    public void set(String uniqueid) {
        this.unique = Uniques.instance().get(uniqueid);
    }

    public List<String> getPossibleEntries(String argument) {
        trie = new Trie();
        if(unique == null) {
            return null;
        }

        for(String modifierid : unique.modifierids()) {
            trie.add(modifierid);
        }

        return trie.match(argument);
    }
}
