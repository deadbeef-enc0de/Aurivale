package net.mctitan.rpg.enums;

import net.mctitan.rpg.spell.instance.*;
import net.mctitan.rpg.spell.type.*;
import net.mctitan.rpg.spell.type.Spell;
import net.mctitan.rpg.spell.instance.SpellInstance;

public enum SpellType {
    BURN_BOLT(new BurnBoltSpell(), BurnBoltInstance.class),
    CALL_LIGHTNING(new CallLightningSpell(), CallLightningInstance.class),
    FIREBALL(new FireballSpell(), FireballInstance.class),
    FROST_BOLT(new FrostBoltSpell(), FrostBoltInstance.class),
    GROUND_SLAM(new GroundSlamSpell(), GroundSlamInstance.class),
    HAVOC_BURST(null, null), // TODO I don't like it, needs a rework
    HEAL(new HealSpell(), HealInstance.class),
    HEALING_BOLT(new HealingBoltSpell(), HealingBoltInstance.class),
    ICE_PRISON(new IcePrisonSpell(), IcePrisonInstance.class),
    LEVITATION(new LevitationSpell(), LevitationInstance.class),
    MOLTEN_TERRAIN(new MoltenTerrainSpell(), MoltenTerrainInstance.class),
    PHANTOM_TUNNEL(new PhantomTunnelSpell(), PhantomTunnelInstance.class),
    SHOCK_BOLT(new ShockBoltSpell(), ShockBoltInstance.class),
    TELEPORT(new TeleportSpell(), TeleportInstance.class),
    VOID_WALK(new VoidWalkSpell(), VoidWalkInstance.class),
    ;

    private Spell spell;
    private Class<? extends SpellInstance> instanceclass;

    SpellType(Spell spell, Class<? extends SpellInstance> instanceclass) {
        this.spell = spell;
        this.instanceclass = instanceclass;
    }

    public Spell spell() { return spell; }
    public Class<? extends SpellInstance> instanceclass() { return instanceclass; }

    public static SpellType spell(String configname) {
        for(SpellType spelltype : values()) {
            if(spelltype.spell() == null) { continue; }
            if(spelltype.spell().configname().equals(configname)) { return spelltype; }
        }
        return null;
    }
}
