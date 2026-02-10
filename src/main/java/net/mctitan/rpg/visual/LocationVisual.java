package net.mctitan.rpg.visual;

import net.mctitan.rpg.util.directional.Location;

public abstract class LocationVisual extends Visual {
    private Location location;

    public LocationVisual() {}

    public LocationVisual(Location location) {
        this.location = location;
    }

    public Location location() { return location; }
}
