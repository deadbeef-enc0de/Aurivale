package net.mctitan.rpg.data.luck;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.LuckModifier;

public class DropLuck extends Modable {
    private double dropluck = 0;
    private double killerluck = 0;

    public int dropluck() { return (int)dropluck; }
    public int killerluck() { return (int)killerluck; }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof LuckModifier luckmod) {
            switch(luckmod.lucktype()) {
                case DROP_LUCK -> {
                    if(luckmod.monster()) {
                        killerluck += luckmod.value();
                    } else {
                        dropluck += luckmod.value();
                    }
                }
            }
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof LuckModifier luckmod) {
            switch(luckmod.lucktype()) {
                case DROP_LUCK -> {
                    if(luckmod.monster()) {
                        killerluck -= luckmod.value();
                    } else {
                        dropluck -= luckmod.value();
                    }
                }
            }
        }
    }
}
