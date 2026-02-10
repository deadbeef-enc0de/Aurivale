package net.mctitan.rpg.commands.completers;

import net.mctitan.commands.TabCompleter;
import net.mctitan.commands.data.Trie;
import net.mctitan.rpg.modifier.Modifiers;
import java.util.List;

public class ModifierNameCompleter implements TabCompleter {
    private static ModifierNameCompleter INSTANCE = new ModifierNameCompleter();
    private Trie trie = new Trie();

    private ModifierNameCompleter() {
        for(String config : Modifiers.instance().configs()) {
            trie.add(config);
        }
    }

    public ModifierNameCompleter(List<String> configs) {
        for(String config : configs) {
            trie.add(config);
        }
    }

    public static ModifierNameCompleter instance() { return INSTANCE; }

    public List<String> getPossibleEntries(String argument) {
        return trie.match(argument);
    }
}
