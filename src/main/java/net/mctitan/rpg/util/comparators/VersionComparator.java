package net.mctitan.rpg.util.comparators;

import net.mctitan.rpg.util.Version;

import java.util.Comparator;

public class VersionComparator implements Comparator<Version> {
    public int compare(Version a, Version b) { return COMPARE(a, b); }

    public static int COMPARE(Version a, Version b) {
        int indices = Math.max(a.length(), b.length());
        for(int index = 0; index < indices; ++index) {
            int ai = a.get(index);
            int bi = b.get(index);
            if(ai != bi) { return ai - bi; }
        }

        return 0;
    }
}
