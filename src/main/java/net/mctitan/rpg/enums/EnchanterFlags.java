package net.mctitan.rpg.enums;

public enum EnchanterFlags {
    MODABLE(true, false),
    NOT_MODABLE(false, false),
    META(false, true)
    ;

    private boolean modable;
    private boolean meta;

    EnchanterFlags(boolean modable, boolean meta) {
        this.modable = modable;
        this.meta = meta;
    }

    public boolean modable() { return modable; }
    public boolean meta() { return meta; }
}
