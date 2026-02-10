package net.mctitan.rpg.data.action;

import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.action.attack.AttackAction;
import net.mctitan.rpg.data.action.potion.PotionAction;
import net.mctitan.rpg.data.action.spell.SpellAction;
import net.mctitan.rpg.data.action.trigger.TriggerAction;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.enums.ActionName;
import net.mctitan.rpg.enums.ActionType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public abstract class Action implements Logger {
    private Entity entity;
    private ActionName name;
    private ActionType actiontype;

    public Action(ActionName name, ActionType actiontype) {
        this.name = name;
        this.actiontype = actiontype;
    }

    public void load(ConfigurationSection section) {
        if(!section.contains("modifiers")) {
            return;
        }

        ConfigurationSection templatessection = section.getConfigurationSection("modifiers");
        for(String templateid : templatessection.getKeys(false)) {
            ConfigurationSection templatesection = templatessection.getConfigurationSection(templateid);
            ModifierTemplate template = ModifierTemplate.template(templatesection);
            if(template != null) {
                loadapply(template.modifier());
            } else {
                log(Level.SEVERE, String.format("Could not load template=%s", templatesection.getName()));
            }
        }
    }

    public Entity entity() { return entity; }
    public ActionName name() { return name; }
    public ActionType actiontype() { return actiontype; }

    public void entity(Entity entity) { this.entity = entity; }

    public void loadapply(Modifier modifier) { apply(modifier); }
    public abstract void apply(Modifier modifier);
    public abstract void unapply(Modifier modifier);

    public abstract void apply(StatusEffect effect);
    public abstract void unapply(StatusEffect effect);

    public static Action create(ActionName name, ActionType actiontype) {
        switch (actiontype) {
            case ATTACK -> { return new AttackAction(name); }
            case POTION -> { return new PotionAction(name); }
            case TRIGGER -> { return new TriggerAction(name); }
            case SPELL -> { return new SpellAction(name); }
        }

        return null;
    }
}
