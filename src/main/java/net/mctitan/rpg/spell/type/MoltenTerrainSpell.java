package net.mctitan.rpg.spell.type;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.spell.instance.MoltenTerrainInstance;
import net.mctitan.rpg.spell.instance.SpellInstance;

public class MoltenTerrainSpell extends Spell {
    private double widthflat;
    private double widthbase;
    private double depthflat;
    private double depthbase;

    @Override
    public void initialize() {
        super.initialize();

        widthflat = section().getDouble("area.width.flat");
        widthbase = section().getDouble("area.width.base");
        depthflat = section().getDouble("area.depth.flat");
        depthbase = section().getDouble("area.depth.base");
    }

    public int width(int level) { return (int)Math.round(widthflat * Math.pow(widthbase, level - 1)); }
    public int depth(int level) { return (int)Math.round(depthflat * Math.pow(depthbase, level - 1)); }

    public String name() { return "Molten Terrain"; }
    public String configname() { return "molten_terrain"; }

    public SpellInstance create(int id, int level, Entity caster, Entity target) {
        return new MoltenTerrainInstance(id, level, duration(level), caster, target);
    }
}
