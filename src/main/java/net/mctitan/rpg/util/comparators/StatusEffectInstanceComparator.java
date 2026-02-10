package net.mctitan.rpg.util.comparators;

import net.mctitan.rpg.data.status.StatusEffectInstance;

import java.util.Comparator;

public class StatusEffectInstanceComparator implements Comparator<StatusEffectInstance> {
    public int compare(StatusEffectInstance a, StatusEffectInstance b) {
        if(a.effect().level() != b.effect().level()) {
            return b.effect().level() - a.effect().level();
        }

        if(a.remaining() != b.remaining()) {
            if(b.effect().permanent()) { return 1; }
            if(a.effect().permanent()) { return -1; }
            return b.remaining() - a.remaining();
        }

        return b.hashCode() - a.hashCode();
    }
}
