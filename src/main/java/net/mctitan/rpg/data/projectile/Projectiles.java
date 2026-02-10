package net.mctitan.rpg.data.projectile;

import net.mctitan.data.BasicData;
import net.mctitan.data.UUID;
import net.mctitan.rpg.data.action.attack.AttackInstance;

import java.util.HashMap;

public class Projectiles extends BasicData {
    private HashMap<UUID, AttackInstance> projectiles = new HashMap<>();
    private transient ProjectileSpeed speed = new ProjectileSpeed();

    public Projectiles() {}

    public ProjectileSpeed speed() { return speed; }

    public boolean has(java.util.UUID uuid) { return has(new UUID(uuid)); }
    public boolean has(UUID uuid) { return projectiles.containsKey(uuid); }

    public AttackInstance get(java.util.UUID projectile) { return get(new UUID(projectile)); }
    public AttackInstance get(UUID projectile) { return projectiles.get(projectile); }

    public void add(java.util.UUID projectile, AttackInstance attackinstance) { add(new UUID(projectile), attackinstance); }
    public void add(UUID projectile, AttackInstance attackinstance) {
        projectiles.put(projectile, attackinstance);
    }

    public void remove(java.util.UUID projectile) { remove(new UUID(projectile)); }
    public void remove(UUID projectile) {
        projectiles.remove(projectile);
    }

    public void removeall() {
        projectiles.clear();
    }
}
