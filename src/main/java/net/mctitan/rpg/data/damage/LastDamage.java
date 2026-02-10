package net.mctitan.rpg.data.damage;

import net.mctitan.data.BasicData;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Player;

import java.util.LinkedList;

public class LastDamage extends BasicData {
    private static final int INSTANCES_KEPT = 3;
    private LinkedList<DamageInstance> instances = new LinkedList<>();

    public LastDamage() {}

    public DamageInstance mostrecent() {
        if(instances.isEmpty()) {
            return null;
        }
        return instances.getFirst();
    }

    public double luck() {
        if(instances.isEmpty()) {
            return 0;
        }

        double damage = 0;
        double luck = 0;
        for(DamageInstance instance : instances) {
            luck += instance.damage() * instance.luck();
            damage += instance.damage();
        }

        return luck / damage;
    }

    public void add(Entity entity, Player player, double damage, double luck) {
        DamageInstance instance = new DamageInstance(player, damage, luck + entity.dropluck().killerluck());
        instances.addFirst(instance);
        if(instances.size() > INSTANCES_KEPT) {
            instances.removeLast();
        }
    }
}
