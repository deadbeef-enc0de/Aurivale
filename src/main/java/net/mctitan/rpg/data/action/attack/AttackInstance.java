package net.mctitan.rpg.data.action.attack;

import net.mctitan.data.BasicData;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.data.damage.Damage;
import net.mctitan.rpg.data.damage.DamageRoll;
import net.mctitan.rpg.enums.Enchantment;

public class AttackInstance extends BasicData {
    private double attackmultiplier;
    private double dropluck;
    private int knockback;
    private double projectilespeed;
    private double splashdamage;
    private boolean sprinting;
    private Cancellation cancellation;
    private Critical critical;
    private Damage damage;
    private Inflictions inflictions;
    private Leech leech;

    public AttackInstance() {}

    public AttackInstance(AttackAction action, Entity entity) {
        attackmultiplier = action.attacktime().swing(entity);
        dropluck = entity.dropluck().dropluck();
        knockback = Math.max(entity.enchantments().level(Enchantment.knockback), entity.enchantments().level(Enchantment.punch));
        projectilespeed = entity.projectiles().speed().value();
        splashdamage = entity.enchantments().level(Enchantment.splash_damage) / 4d;
        sprinting = (entity instanceof Player player && player.bukkitplayer().isSprinting());

        cancellation = action.cancellation().clone();
        critical = action.critical().clone();
        damage = action.damage().clone();
        inflictions = action.inflictions().clone();
        leech = action.leech().clone();
    }

    public DamageRoll roll() {
        return damage.roll().multiply(attackmultiplier);
    }

    public boolean hassplash() { return splashdamage > 0; }

    public double attackmultiplier() { return attackmultiplier; }
    public double dropluck() { return dropluck; }
    public int knockback() { return knockback; }
    public double projectilespeed() { return projectilespeed; }
    public double splashdamage() { return splashdamage; }
    public boolean sprinting() { return sprinting; }
    public Cancellation cancellation() { return cancellation; }
    public Critical critical() { return critical; }
    public Inflictions inflictions() { return inflictions; }
    public Leech leech() { return leech; }

    public void attackmultiplier(double attackmultiplier) { this.attackmultiplier = attackmultiplier; }
}
