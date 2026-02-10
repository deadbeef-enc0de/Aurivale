package net.mctitan.rpg.loot.drops;

import net.mctitan.rpg.crafting.Craftables;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.enums.ModifierSlot;
import net.mctitan.rpg.enums.SpellActivation;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.loot.Drop;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.SpellTemplate;
import net.mctitan.rpg.util.Math;

import java.util.ArrayList;
import java.util.List;

public class WandDrop implements Drop {
    private static final String WAND_CRAFTABLE = "wand";
    private static final List<SpellType> spelltypes = new ArrayList<>();
    private final int level;

    public WandDrop(int level) {
        this.level = level;
    }

    public ItemStack stack(int luck) {
        // get wand item stack
        ItemStack wand = Craftables.instance().craftable(WAND_CRAFTABLE).create(luck / 2);

        // get spell type
        SpellType spelltype = spelltypes.get(Math.nextInt(spelltypes.size()));

        // add modifier to wand
        String id = String.format("wand_spell_%s", spelltype.spell().configname());
        ModifierTemplate template = new SpellTemplate(level, true, false, id,
                spelltype, SpellActivation.CAST, 0, level, 0);
        template.modifier().apply(ModifierSlot.BASE, wand);

        // hand back the wand
        return wand;
    }

    static {
        for(SpellType spelltype : SpellType.values()) {
            // skip spells that are not finished
            if(spelltype.spell() == null || spelltype.instanceclass() == null) {
                continue;
            }

            // add spell to possible choices
            spelltypes.add(spelltype);
        }
    }
}
