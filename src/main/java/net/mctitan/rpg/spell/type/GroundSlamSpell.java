package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.spell.instance.GroundSlamInstance;
import net.mctitan.rpg.spell.instance.SpellInstance;

public class GroundSlamSpell extends Spell {
    public static final String NEAR = "near";
    public static final String CLOSE = "close";
    public static final String FAR = "far";

    private double distanceflat;
    private double distancebase;

    private int nearheight;
    private double nearmulti;
    private int closeheight;
    private double closemulti;
    private int farheight;
    private double farmulti;

    private DamageType damagetype;
    private double mindamageflat;
    private double mindamagebase;
    private double maxdamageflat;
    private double maxdamagebase;

    @Override
    public void initialize() {
        super.initialize();

        distanceflat = section().getDouble("distance.flat");
        distancebase = section().getDouble("distance.base");
        nearheight = section().getInt("distances.near.height");
        nearmulti = section().getDouble("distances.near.damage_multi");
        closeheight = section().getInt("distances.close.height");
        closemulti = section().getDouble("distances.close.damage_multi");
        farheight = section().getInt("distances.far.height");
        farmulti = section().getDouble("distances.far.damage_multi");
        damagetype = DamageType.valueOf(section().getString("damage.type"));
        mindamageflat = section().getDouble("damage.minimum.flat");
        mindamagebase =  section().getDouble("damage.minimum.base");
        maxdamageflat = section().getDouble("damage.maximum.flat");
        maxdamagebase =  section().getDouble("damage.maximum.base");
    }

    public double distance(int level) { return distanceflat * Math.pow(distancebase, level - 1); }
    public DamageType damagetype() { return damagetype; }
    public double mindamage(int level) { return mindamageflat * Math.pow(mindamagebase, level - 1); }
    public double maxdamage(int level) { return maxdamageflat * Math.pow(maxdamagebase, level - 1); }

    public int height(String distance) {
        switch(distance) {
            case NEAR -> { return nearheight; }
            case CLOSE -> { return closeheight; }
            case FAR -> { return farheight; }
        }

        return -1;
    }

    public double damagemulti(String distance) {
        switch(distance) {
            case NEAR -> { return nearmulti; }
            case CLOSE -> { return closemulti; }
            case FAR -> { return farmulti; }
        }

        return 0;
    }

    public String name() { return "Ground Slam"; }
    public String configname() { return "ground_slam"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new GroundSlamInstance(id, level, duration(level), caster, target);
    }
}
