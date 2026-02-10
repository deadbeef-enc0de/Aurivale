package net.mctitan.rpg.util.teleporter;

import net.mctitan.rpg.util.directional.Location;

public interface LocationSequence {
    Location current();
    void next();
}
