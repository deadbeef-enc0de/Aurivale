package net.mctitan.rpg.data.random;

import net.mctitan.data.BasicData;
import net.mctitan.rpg.enums.RandomType;
import net.mctitan.rpg.util.Math;

import java.util.HashMap;

public class Randoms extends BasicData {
    private HashMap<RandomType, ReproducibleRandom> types = new HashMap<>();

    public Randoms() {}

    public ReproducibleRandom random(RandomType type) {
        if(!types.containsKey(type)) {
            types.put(type, new ReproducibleRandom(Math.random().nextLong()));
        }

        return types.get(type);
    }
}
