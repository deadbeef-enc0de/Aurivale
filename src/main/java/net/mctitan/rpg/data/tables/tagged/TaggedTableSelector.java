package net.mctitan.rpg.data.tables.tagged;

import java.util.*;

public class TaggedTableSelector {
    public static final double MUCH_LESS_LIKELY = 0.05;
    public static final double LESS_LIKELY = 0.1;
    public static final double NORMAL = 1;
    public static final double MORE_LIKELY = 10;
    public static final double MUCH_MORE_LIKELY = 20;

    public TaggedTableSelector clone() {
        TaggedTableSelector ret = new TaggedTableSelector();
        ret.whitelist = new HashSet<>(this.whitelist);
        ret.blacklist = new HashSet<>(this.blacklist);
        ret.likelyhood = new HashMap<>(this.likelyhood);

        return ret;
    }

    private Set<String> whitelist = new HashSet<>();
    private Set<String> blacklist = new HashSet<>();
    Map<String, Double> likelyhood = new HashMap<>();

    public double likelyhood(String tag) {
        if(!likelyhood.containsKey(tag)) { return NORMAL; }
        return likelyhood.get(tag);
    }

    public boolean matches(List<String> tags) {
        for(String tag : tags) {
            if(!whitelist.isEmpty() && whitelist.contains(tag)) { return true; }
            if(blacklist.contains(tag)) { return false; }
        }

        return whitelist.isEmpty();
    }

    public void whitelist(String tag) { whitelist.add(tag); }
    public void blacklist(String tag) { blacklist.add(tag); }
    public void likelyhood(String tag, double modifier) {
        this.likelyhood.put(tag, modifier);
    }

    public Set<String> whitelist() { return new HashSet<>(whitelist); }
    public Set<String> blacklist() { return new HashSet<>(blacklist); }
}
