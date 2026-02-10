package net.mctitan.rpg.enums;

public enum Enchantment {
    // minecraft enchantments
    aqua_affinity("Aqua Affinity", org.bukkit.enchantments.Enchantment.AQUA_AFFINITY),
    channeling("Channeling", org.bukkit.enchantments.Enchantment.CHANNELING),
    depth_strider("Depth Strider", org.bukkit.enchantments.Enchantment.DEPTH_STRIDER),
    efficiency("Efficiency", org.bukkit.enchantments.Enchantment.EFFICIENCY),
    feather_falling("Feather Falling", org.bukkit.enchantments.Enchantment.FEATHER_FALLING),
    fortune("Fortune", org.bukkit.enchantments.Enchantment.FORTUNE),
    frost_walker("Frost Walker", org.bukkit.enchantments.Enchantment.FROST_WALKER),
    infinity("Infinity", org.bukkit.enchantments.Enchantment.INFINITY),
    knockback("Knockback", org.bukkit.enchantments.Enchantment.KNOCKBACK),
    loyalty("Loyalty", org.bukkit.enchantments.Enchantment.LOYALTY),
    multishot("Multishot", org.bukkit.enchantments.Enchantment.MULTISHOT),
    piercing("Piercing", org.bukkit.enchantments.Enchantment.PIERCING),
    punch("Punch", org.bukkit.enchantments.Enchantment.PUNCH),
    quick_charge("Quick Charge", org.bukkit.enchantments.Enchantment.QUICK_CHARGE),
    respiration("Respiration", org.bukkit.enchantments.Enchantment.RESPIRATION),
    riptide("Riptide", org.bukkit.enchantments.Enchantment.RIPTIDE),
    silk_touch("Silk Touch", org.bukkit.enchantments.Enchantment.SILK_TOUCH),
    soul_speed("Soul Speed", org.bukkit.enchantments.Enchantment.SOUL_SPEED),
    unbreaking("Unbreaking", org.bukkit.enchantments.Enchantment.UNBREAKING),

    // Aurivale custom enchantments
    projectile_rider("Projectile Rider", 1), // shooter rides arrow/rocket fired from bow/crossbow
    effect_area("Effect Area", 3), // increases the area of effect of the tool
    tree_feller("Tree Feller", 1), // removes entire tree when breaking a single block
    smelting("Smelting", 5), // gives 25% chance to smelt drop per level
    item_bag("Item Bag", 1), // automatically moves items to bag from broken blocks
    splash_damage("Splash Damage", 4), // adds splash damage to non-sword weapons
    enemy_explode_on_death("Enemies Explode", 3), // 10% chance for enemy to explode on death
    straight_shot("Straight Shot", 1),

    // used has a placeholder for showing the enchantment shine for items without a enchantment
    mending("Mending", org.bukkit.enchantments.Enchantment.MENDING),
    ;

    String display;
    int maxlevel = -1;
    private org.bukkit.enchantments.Enchantment bukkit = null;

    private Enchantment(String display, int maxlevel) {
        this.display = display;
        this.maxlevel = maxlevel;
    }

    private Enchantment(String display, org.bukkit.enchantments.Enchantment bukkit) {
        this.display = display;
        this.bukkit = bukkit;
    }

    public int maxlevel() {
        if(bukkit != null) { return bukkit.getMaxLevel(); }
        return maxlevel;
    }

    public String display() { return display; }
    public org.bukkit.enchantments.Enchantment bukkit() { return bukkit; }
}
