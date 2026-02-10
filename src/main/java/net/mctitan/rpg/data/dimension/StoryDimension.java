package net.mctitan.rpg.data.dimension;

import net.mctitan.rpg.enums.StoryType;

public class StoryDimension extends Dimension {
    private StoryType type;
    private int tier;

    public StoryDimension() {}

    public StoryType type() { return type; }
    public int tier() { return tier; }
}
