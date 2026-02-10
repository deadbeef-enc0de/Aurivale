package net.mctitan.rpg.data;

import net.mctitan.data.UUID;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.mana.Mana;
import net.mctitan.rpg.data.message.ActionMessageBar;
import net.mctitan.rpg.data.message.types.AggroProtectionMessage;
import net.mctitan.rpg.data.skill.Skill;
import net.mctitan.rpg.enums.EnchantingState;
import net.mctitan.rpg.enums.SkillType;
import net.mctitan.rpg.event.PlayerStatsUpdateEvent;
import net.mctitan.rpg.util.Version;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Projectile;

import java.util.Map;
import java.util.logging.Level;

public class Player extends Entity {
    private transient Map<SkillType, Skill> skills = Map.of(
            SkillType.BREWING, new Skill(),
            SkillType.CRAFTING, new Skill(),
            SkillType.ENCHANTING, new Skill()
    );

    private UUID party;
    private ActionMessageBar actionbar = new ActionMessageBar();
    private EnchantingState enchatingstate = EnchantingState.NOT_ENCHANTING;
    private Mana mana = new Mana();
    private Version handbook;
    private boolean isnew = true;
    private boolean aggroprotect = false;

    public Player() {}

    public Player(org.bukkit.entity.Player player) {
        super(player);

        // add aggro protection message
        int aggroprotection = Aurivale.instance().getConfig("players").getInt("aggro_protection");
        setaggroprotection(aggroprotection);

        // set current health to max health, this is done again as the player might have a modifier attached
        health = maxhealth();
    }

    @Override
    public void initialize() {
        super.initialize();
        log(Level.INFO, "Player::initialize()");

        // set base attack speed to 1/tick, if something is equipped it will be overridden
        bukkitplayer().getAttribute(Attribute.ATTACK_SPEED).setBaseValue(20);
    }

    @Override public org.bukkit.entity.LivingEntity bukkitentity() { return bukkitplayer(); }
    public org.bukkit.entity.Player bukkitplayer() {
        return Bukkit.getPlayer(this.uuid.uuid());
    }

    public Skill skill(SkillType type) { return this.skills.get(type); }
    public Skill brewing() { return skills.get(SkillType.BREWING); }
    public Skill crafting() { return skills.get(SkillType.CRAFTING); }
    public Skill enchanting() { return skills.get(SkillType.ENCHANTING); }

    public Party party() { return (party == null ? null : DataManager.instance().party(party)); }
    public ActionMessageBar actionbar() { return actionbar; }
    public EnchantingState enchantingstate() { return enchatingstate; }
    public Mana mana() { return mana; }
    public Version handbook() { return handbook; }
    public boolean isnew() { return isnew; }
    public boolean aggroprotect() { return aggroprotect; }

    public void handbook(Version handbook) { this.handbook = handbook; }
    public void isnew(boolean isnew) { this.isnew = isnew; }
    public void party(Party party) { this.party = (party == null ? null : party.uuid); }

    public void enchantingstate(EnchantingState enchatingstate) { this.enchatingstate = enchatingstate; }

    public boolean canBlock(Entity damager, Projectile projectile) {
        Location playerloc = bukkitplayer().getLocation();
        Location damagerloc = damager.bukkitentity().getLocation();
        if(projectile != null) { damagerloc = projectile.getLocation(); }

        return playerloc.getDirection().angle(damagerloc.subtract(playerloc).toVector()) <= (Math.PI / 2);
    }

    public void showmana(double mana, double maxmana) {
        float bar = maxmana > 0 ? (float) (mana / maxmana) : 0;
        bukkitplayer().setExp(bar);
        bukkitplayer().setLevel((int)mana);
    }

    @Override
    public void damage(double damage) {
        super.damage(damage);

        PlayerStatsUpdateEvent event = new PlayerStatsUpdateEvent(this);
        event.callEvent();
    }

    @Override
    public void heal(double damage) {
        super.heal(damage);

        PlayerStatsUpdateEvent event = new PlayerStatsUpdateEvent(this);
        event.callEvent();
    }

    public void setaggroprotection(int time) {
        if(aggroprotect) {
            return;
        }

        aggroprotect = true;
        actionbar.add(new AggroProtectionMessage(time));
    }

    public void removeaggroprotection() {
        aggroprotect = false;
    }
}
