package net.mctitan.rpg.data.tables.tagged.selectors;

import net.mctitan.rpg.data.tables.tagged.TaggedTableSelector;

public class SuffixSelector {
    private static TaggedTableSelector selector = null;

    public static TaggedTableSelector selector() {
        if (selector == null) {
            selector = new TaggedTableSelector();
            selector.whitelist("suffix");
        }
        return selector;
    }
}
