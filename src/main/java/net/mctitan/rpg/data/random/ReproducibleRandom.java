package net.mctitan.rpg.data.random;

import net.mctitan.data.BasicData;

import java.util.Random;

public class ReproducibleRandom extends BasicData {
    private long seed;
    private long current;

    public ReproducibleRandom() {}

    public ReproducibleRandom(long seed) {
        this.seed = seed;
        reset();
    }

    private Random random(long seed) { return new Random(seed); }

    public Random random() {
        Random random = random(current);
        current = random.nextLong();
        return random;
    }

    public final void reset() {
        current = random(seed).nextLong();
    }
}
