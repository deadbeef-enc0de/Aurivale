package net.mctitan.rpg.spell.type.templates;

import net.mctitan.rpg.enums.DamageType;
import net.mctitan.rpg.enums.EffectType;

public abstract class DamagingBoltSpell extends BoltSpell {
    private DamageType damagetype;
    private double mindamageflat;
    private double mindamagebase;
    private double maxdamageflat;
    private double maxdamagebase;

    private EffectType effecttype;
    private int effectlevel;

    private double effectdurationflat;
    private double effectdurationbase;

    @Override
    public void initialize() {
        super.initialize();

        damagetype = DamageType.valueOf(section().getString("damage.type"));
        mindamageflat = section().getDouble("damage.minimum.flat");
        mindamagebase =  section().getDouble("damage.minimum.base");
        maxdamageflat = section().getDouble("damage.maximum.flat");
        maxdamagebase =  section().getDouble("damage.maximum.base");

        if(section().contains("effect")) {
            effecttype = EffectType.valueOf(section().getString("effect.type"));
            effectlevel = section().getInt("effect.level");
            effectdurationflat = section().getDouble("effect.duration.flat");
            effectdurationbase = section().getDouble("effect.duration.base");
        }
    }

    public DamageType damagetype() { return damagetype; }
    public double mindamage(int level) { return mindamageflat * Math.pow(mindamagebase, level - 1); }
    public double maxdamage(int level) { return maxdamageflat * Math.pow(maxdamagebase, level - 1); }

    public boolean haseffect() { return effecttype != null; }
    public EffectType effecttype() { return effecttype; }
    public int effectlevel() { return effectlevel; }
    public double effectduration(int level) { return effectdurationflat * Math.pow(effectdurationbase, level - 1); }
}
