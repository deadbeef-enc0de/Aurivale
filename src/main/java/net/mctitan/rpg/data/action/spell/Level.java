package net.mctitan.rpg.data.action.spell;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.LevelModifier;

import java.util.HashMap;
import java.util.Map;

public class Level extends Modable {
    private double levelflat = 0;
    private double levelscaler = 1;
    private Map<Modifier, Double> levelmultis = new HashMap<>();

    public int value(int original) {
        double ret = (original + levelflat) * levelscaler;
        for(Double multi : levelmultis.values()) {
            ret *= (1 + multi);
        }

        return (int)ret;
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof LevelModifier levelmod) {
            switch (levelmod.operator()) {
                case FLAT -> { levelflat += levelmod.value(); }
                case SCALER -> { levelscaler += levelmod.value() / 100; }
                case MULTIPLIER -> { levelmultis.put(modifier, levelmod.value() / 100); }
            }
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof LevelModifier levelmod) {
            switch (levelmod.operator()) {
                case FLAT -> { levelflat -= levelmod.value(); }
                case SCALER -> { levelscaler -= levelmod.value() / 100; }
                case MULTIPLIER -> { levelmultis.remove(modifier); }
            }
        }
    }
}
