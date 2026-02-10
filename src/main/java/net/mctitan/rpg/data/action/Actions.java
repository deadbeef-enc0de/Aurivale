package net.mctitan.rpg.data.action;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.data.action.attack.AttackAction;
import net.mctitan.rpg.data.action.potion.PotionAction;
import net.mctitan.rpg.data.action.spell.SpellAction;
import net.mctitan.rpg.data.action.trigger.TriggerAction;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.enums.ActionName;
import net.mctitan.rpg.enums.ActionType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

public class Actions extends Modable implements Logger {
    private Entity entity;
    private Map<ActionName, Action> actions = new HashMap<>();
    private Map<ActionType, LinkedList<Action>> actiontypes = new HashMap<>();

    public Actions() {
        for(ActionType type : ActionType.values()) {
            actiontypes.put(type, new LinkedList<>());
        }

        // add some default actions every entity needs
        addaction(ActionName.DEFAULT, ActionType.ATTACK);
        addaction(ActionName.TRIGGER, ActionType.TRIGGER);
        addaction(ActionName.SPELLS, ActionType.SPELL);
    }

    public void entity(Entity entity) {
        this.entity = entity;
        for(Action action : actions.values()) {
            action.entity(entity);
        }
    }

    public int count() { return actions.size(); }

    public Action action(ActionName name) { return actions.get(name); }

    public AttackAction attack() { return (AttackAction)action(ActionName.DEFAULT); }

    public AttackAction attack(EntityDamageByEntityEvent event) {
        // start with default action
        Action action = action(ActionName.DEFAULT);

        // check baby
        if(event.getDamager() instanceof Ageable ageable &&
           !ageable.isAdult()) {
            action = action(ActionName.BABY);
        }

        // check creeper
        if(event.getDamager().getType() == EntityType.CREEPER &&
           event.getDamager() instanceof Creeper creeper &&
           creeper.isPowered()) {
            action = action(ActionName.CHARGED_EXPLOSION);
        }

        // check slime and magma cube
        if(event.getDamager().getType() == EntityType.SLIME ||
           event.getDamager().getType() == EntityType.MAGMA_CUBE) {
            Slime slime = (Slime)event.getDamager();
            action = action(ActionName.slime(slime.getSize()));
        }

        // check warden sonic boom
        else if(event.getDamager().getType() == EntityType.WARDEN &&
                event.getCause() == DamageCause.SONIC_BOOM) {
            action = action(ActionName.SONIC_BOOM);
        }

        // check wither birthing explosion
        else if(event.getDamager().getType() == EntityType.WITHER &&
                event.getCause() == DamageCause.ENTITY_EXPLOSION) {
            action = action(ActionName.WITHER_BIRTH);
        }

        //return the attack action
        if(action == null || action.actiontype() != ActionType.ATTACK) {
            return null;
        }
        return (AttackAction)action;
    }

    public AttackAction attack(Entity shooter, Projectile projectile) {
        // start with default action
        Action action = action(ActionName.DEFAULT);

        // test for drowned thrown trident
        if(projectile.getType() == EntityType.TRIDENT &&
                shooter.bukkitentity().getType() == EntityType.DROWNED) {
            action = shooter.actions().action(ActionName.TRIDENT_THROW);
        }

        // test for wither skull projectile
        if(projectile.getType() == EntityType.WITHER_SKULL) {
            action = shooter.actions().action(ActionName.WITHER_SKULL);
        }

        //return the attack action
        if(action == null || action.actiontype() != ActionType.ATTACK) {
            return null;
        }
        return (AttackAction)action;
    }

    public TriggerAction trigger() {
        // start with the trigger action
        Action action = action(ActionName.TRIGGER);

        // return the action
        if(action == null || action.actiontype() != ActionType.TRIGGER) {
            return null;
        }
        return (TriggerAction)action;
    }

    public SpellAction spell() {
        //start with the spells action
        Action action = action(ActionName.SPELLS);

        // return the action
        if(action == null || action.actiontype() != ActionType.SPELL) {
            return null;
        }
        return (SpellAction)action;
    }

    public PotionAction potion(PotionType potiontype) {
        // get the action name for the potion type
        ActionName name = ActionName.potion(potiontype);
        if(name == null) {
            return null;
        }

        // return the action
        return (PotionAction) actions.get(name);
    }

    public Action addaction(String namestr, ConfigurationSection section) {
        ActionName actionname = ActionName.actionname(namestr);
        if(actionname == null) {
            log(Level.WARNING, String.format("ActionName %s does not exist", namestr));
            return null;
        }

        if(!section.contains("type")) {
            log(Level.WARNING, String.format("Action %s does not have a type", actionname.name()));
            return null;
        }

        ActionType actiontype = ActionType.valueOf(section.getString("type"));
        Action action = Action.create(actionname, actiontype);
        if(action == null) {
            log(Level.WARNING, String.format("Action with type=%s could not be made", actiontype));
            return null;
        }

        action.load(section);
        action.entity(entity);
        actions.put(actionname, action);
        actiontypes.get(actiontype).add(action);
        return action;
    }

    public void addaction(ActionName name, ActionType actiontype) {
        Action action = Action.create(name, actiontype);
        if(action == null) {
            log(Level.SEVERE, String.format("Cannot add action for name=%s actiontype=%s", name, actiontype));
            return;
        }

        action.entity(entity);
        actions.put(name, action);
        actiontypes.get(actiontype).add(action);
    }

    public void add(Modifier modifier) {
        for(Action action : actions.values()) {
            action.apply(modifier);
        }
    }

    public void remove(Modifier modifier) {
        for(Action action : actions.values()) {
            action.unapply(modifier);
        }
    }

    public void apply(StatusEffect effect) {
        for(Action action : actions.values()) {
            action.apply(effect);
        }
    }

    public void unapply(StatusEffect effect) {
        for(Action action : actions.values()) {
            action.unapply(effect);
        }
    }
}
