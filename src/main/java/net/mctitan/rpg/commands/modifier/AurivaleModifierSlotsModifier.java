package net.mctitan.rpg.commands.modifier;

import net.mctitan.commands.arguments.Argument;
import net.mctitan.commands.arguments.OptionalArgument;
import net.mctitan.rpg.commands.AurivaleBaseCommand;
import net.mctitan.rpg.commands.completers.CraftableCompleter;
import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.tables.tagged.TaggedTable;
import net.mctitan.rpg.data.tables.tagged.TaggedTableEntry;
import net.mctitan.rpg.data.tables.tagged.TaggedTableSelector;
import net.mctitan.rpg.data.tables.tagged.selectors.PrefixSelector;
import net.mctitan.rpg.data.tables.tagged.selectors.SuffixSelector;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.util.Logger;

import java.util.*;
import java.util.logging.Level;

public class AurivaleModifierSlotsModifier extends AurivaleBaseCommand implements Logger {
    private final Argument<Craftable> craftable = new Argument<>("craftable", Craftable.class);
    private final OptionalArgument<String> tags = new OptionalArgument<>("tags", String.class, "");

    public AurivaleModifierSlotsModifier() {
        super("slots");

        this.craftable.completer(new CraftableCompleter());

        this.add(craftable);
        this.add(tags);
    }

    public boolean runCommand(Player player) {
        Craftable craftable = this.craftable.value();
        if(craftable == null) {
            return true;
        }

        // add tag selector
        TaggedTableSelector selector = null;
        if(tags.value() != null && !tags.value().isEmpty()) {
            selector = new TaggedTableSelector();
            for(String tag : tags.value().split(",")) {
                selector.whitelist(tag);
            }
        }

        // get all prefixes
        int allprefixweights = 0;
        int longestprefixstr = 0;
        Map<String, Integer> prefixweights = new TreeMap<>();
        NavigableSet<String> prefixstrs = new TreeSet<>();
        TaggedTable<ModifierTemplate> prefixes = craftable.magicmodifiers().subset(PrefixSelector.selector());
        if(selector != null) { prefixes = prefixes.subset(selector); }
        for(TaggedTableEntry<ModifierTemplate> prefix : prefixes.taggedentries()) {
            allprefixweights += prefix.weight();
            if(!prefixweights.containsKey(prefix.group())) { prefixweights.put(prefix.group(), 0); }
            prefixweights.put(prefix.group(), prefixweights.get(prefix.group()) + prefix.weight());
        }

        // get all suffixes
        int allsuffixweights = 0;
        int longestsuffixstr = 0;
        Map<String, Integer> suffixweights = new TreeMap<>();
        NavigableSet<String> suffixstrs = new TreeSet<>();
        TaggedTable<ModifierTemplate> suffixes = craftable.magicmodifiers().subset(SuffixSelector.selector());
        if(selector != null) { suffixes = suffixes.subset(selector); }
        for(TaggedTableEntry<ModifierTemplate> suffix : suffixes.taggedentries()) {
            allsuffixweights += suffix.weight();
            if(!suffixweights.containsKey(suffix.group())) { suffixweights.put(suffix.group(), 0); }
            suffixweights.put(suffix.group(), suffixweights.get(suffix.group()) + suffix.weight());
        }

        // get all modifier strings
        int totalweights = allprefixweights + allsuffixweights;
        double prefixpercent = 100f * allprefixweights / totalweights;
        double suffixpercent = 100f * allsuffixweights / totalweights;
        for(Map.Entry<String, Integer> entry : prefixweights.entrySet()) {
            String str = String.format("% 4d: %s", entry.getValue(), entry.getKey());
            prefixstrs.add(str);
            if(str.length() > longestprefixstr) {
                longestprefixstr = str.length();
            }
        }
        for(Map.Entry<String, Integer> entry : suffixweights.entrySet()) {
            String str = String.format("% 4d: %s", entry.getValue(), entry.getKey());
            suffixstrs.add(str);
            if(str.length() > longestsuffixstr) {
                longestsuffixstr = str.length();
            }
        }

        // output lines
        List<String> lines = new LinkedList<>();

        // setup total prefixes/suffixes
        String prefixline = String.format("Prefixes %d %.00f%%", allprefixweights, prefixpercent);
        longestprefixstr = Math.max(longestprefixstr, prefixline.length());
        prefixline = center(prefixline, longestprefixstr);

        String suffixline = String.format("Suffixes %d %.00f%%", allsuffixweights, suffixpercent);
        longestsuffixstr = Math.max(longestsuffixstr, suffixline.length());
        suffixline = center(suffixline, longestsuffixstr);

        int width = longestprefixstr + 3 + longestsuffixstr;

        // setup headers
        lines.add(" ");
        lines.add(repeat(width, "="));
        lines.add(center(craftable.displayname(), width));
        if(craftable.enchantingbonus() != 0) {
            lines.add(center(String.format("Enchanting Bonus: %d", craftable.enchantingbonus()), width));
        }
        lines.add(center(String.format("Total Weight: %d", totalweights), width));
        lines.add(repeat(width, "="));
        lines.add(String.format("%s | %s", prefixline, suffixline));
        lines.add(repeat(width, "-"));

        // show prefixes/suffixes
        String[] prefixstrsarray = prefixstrs.reversed().toArray(new String[0]);
        String[] suffixstrsarray = suffixstrs.reversed().toArray(new String[0]);
        int length = Math.max(prefixstrs.size(), suffixstrs.size());
        for(int index = 0; index < length; ++index) {
            String prefix = left(index < prefixstrsarray.length ? prefixstrsarray[index] : "", longestprefixstr);
            String suffix = left(index < suffixstrsarray.length ? suffixstrsarray[index] : "", longestsuffixstr);
            lines.add(String.format("%s | %s", prefix, suffix));
        }
        lines.add(repeat(width, "-"));

        // Log the lines all in one
        log(Level.INFO, String.join("\n", lines));

        return true;
    }

    private String left(String str, int len) {
        return String.format("%s%s", str, spaces(len-str.length()));
    }

    private String center(String str, int len) {
        return String.format("%s%s%s", spaces((len-str.length())/2), str, spaces((len-str.length()+1)/2));
    }

    private String spaces(int amount) { return repeat(amount, " "); }
    private String repeat(int amount, String s) { return String.join("", Collections.nCopies(amount, s)); }
}
