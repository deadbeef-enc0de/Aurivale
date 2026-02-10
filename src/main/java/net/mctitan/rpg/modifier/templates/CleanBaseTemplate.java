package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.CleanBaseModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Random;

public class CleanBaseTemplate extends ModifierTemplate {
    public CleanBaseTemplate(int rank, boolean base, boolean monster, String id) {
        super(rank, base, monster, id);
    }

    public CleanBaseTemplate(PersistentDataContainer pdc) {
        super(pdc);
    }

    public CleanBaseTemplate(ConfigurationSection section) {
        super(section);
    }

    @Override
    public Modifier modifier(Random random) {
        return new CleanBaseModifier(rank(), this, base(), monster());
    }

    @Override
    public String string() { return "Clean Base Modifiers"; }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);
    }
}
