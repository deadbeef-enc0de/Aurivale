package net.mctitan.rpg.data.cooldown;

import net.mctitan.data.BasicData;
import net.mctitan.data.UUID;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class PotionCooldown extends BasicData implements Comparable<PotionCooldown> {
    private UUID uuid;
    private int cooldown;

    public PotionCooldown() {}

    public PotionCooldown(UUID uuid, Integer cooldown) {
        this.uuid = uuid;
        this.cooldown = cooldown;
    }

    public UUID uuid() { return uuid; }
    public int cooldown() { return cooldown; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotionCooldown other = (PotionCooldown) o;

        return uuid.equals(other.uuid) && cooldown == other.cooldown;
    }

    @Override
    public int compareTo(@NotNull PotionCooldown other) {
        // check cooldown difference
        if(this.cooldown < other.cooldown) { return -1; }
        if(this.cooldown > other.cooldown) { return 1; }

        // sort on uuid as a backup
        return this.uuid.uuid().compareTo(other.uuid.uuid());
    }

    public static class PotionCooldownComparator implements Comparator<PotionCooldown> {
        @Override
        public int compare(PotionCooldown a, PotionCooldown b) {
            return a.compareTo(b);
        }
    }
}
