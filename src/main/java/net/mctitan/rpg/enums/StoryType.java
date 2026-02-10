package net.mctitan.rpg.enums;

public enum StoryType {
    DESERT,
    JUNGLE,
    PLAINS,
    RIVER,
    ;

    private final String printname;
    private final String configname;

    StoryType() {
        printname = this.name().substring(0,1)+this.name().substring(1).toLowerCase();
        configname = this.name().toLowerCase();
    }

    public String printname() { return printname; }
    public String configname() { return configname; }
}
