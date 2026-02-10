package net.mctitan.rpg.data.action.potion;

import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.crafting.Craftables;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.action.Action;
import net.mctitan.rpg.data.status.StatusEffect;
import net.mctitan.rpg.enums.ActionName;
import net.mctitan.rpg.enums.ActionType;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.util.Logger;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public class PotionAction extends Action {
    private ItemStack potion = null;

    public PotionAction(ActionName name) { super(name, ActionType.POTION); }

    public ItemStack potion() { return potion; }

    @Override public void load(ConfigurationSection section) {
        // create potion craftable object
        Craftable craftable = Craftables.instance().craftable(section.getString("craftable"));
        if(craftable != null) {
            potion = craftable.create();
        }

        // call super class load
        super.load(section);
    }

    @Override public void loadapply(Modifier modifier) {
        if(potion == null || modifier.rawtemplate() == null) { return; }
        Logger.LOG(Level.INFO, String.format("%s applying modifier %s", this.name(), modifier.id()));
        modifier.apply(ModifierSlot.IMPLICIT, potion);
    }

    // we don't care about dynamic modifiers or status effects for potions
    public void apply(Modifier modifier) {}
    public void unapply(Modifier modifier) {}
    public void apply(StatusEffect effect) {}
    public void unapply(StatusEffect effect) {}
}
