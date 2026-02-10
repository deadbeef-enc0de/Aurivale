package net.mctitan.rpg.data.damage;

import net.mctitan.data.BasicData;
import net.mctitan.data.UUID;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Player;

public class DamageInstance extends BasicData {
    private UUID damager;
    private double damage;
    private double luck;

    public DamageInstance() {} // for YamlSaver

    public DamageInstance(Player player, double damage, double luck) {
        this.damager = player.uuid;
        this.damage = damage;
        this.luck = luck;
    }

    public Player player() { return DataManager.instance().player(damager); }
    public double damage() { return damage; }
    public double luck() { return luck; }
}
