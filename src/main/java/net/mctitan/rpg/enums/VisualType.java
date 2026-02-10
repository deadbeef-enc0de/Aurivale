package net.mctitan.rpg.enums;

import net.mctitan.rpg.visual.Visual;
import net.mctitan.rpg.visual.type.EntityShine;

public enum VisualType {
    ENTITY_SHINE(EntityShine.class),
    ;

    private Class<? extends Visual> visualclass;

    VisualType(Class<? extends Visual> visualclass) {
        this.visualclass = visualclass;
    }

    public Class<? extends Visual> visualclass() { return visualclass; }
}
