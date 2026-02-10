package net.mctitan.rpg.visual;

import net.mctitan.data.BaseData;
import net.mctitan.rpg.enums.VisualType;
import net.mctitan.rpg.util.directional.Location;

public abstract class Visual extends BaseData {
    public abstract VisualType type();
    public abstract Location location();
    public abstract void initialize();
    public abstract void tick();
}
