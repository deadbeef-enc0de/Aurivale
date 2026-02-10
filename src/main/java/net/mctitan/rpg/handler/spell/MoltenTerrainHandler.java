package net.mctitan.rpg.handler.spell;

import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.SpellChangedBlock;
import net.mctitan.rpg.spell.Spells;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;

public class MoltenTerrainHandler implements Listener {
    private static final MoltenTerrainHandler instance = new MoltenTerrainHandler();

    private MoltenTerrainHandler() {}

    public static MoltenTerrainHandler instance() { return instance; }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        SpellChangedBlock block = Spells.instance().block(event.getBlock().getLocation());
        if(block != null && block.recent() != null && block.recent().spelltype() == SpellType.MOLTEN_TERRAIN) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        // test lava moving
        SpellChangedBlock block = Spells.instance().block(event.getBlock().getLocation());
        if(block != null && block.recent() != null && block.recent().spelltype() == SpellType.MOLTEN_TERRAIN) {
            event.setCancelled(true);
        }

        // test something moving into the lava
        block = Spells.instance().block(event.getToBlock().getLocation());
        if(block != null && block.recent() != null && block.recent().spelltype() == SpellType.MOLTEN_TERRAIN) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if(event.getIgnitingBlock() == null) {
            return;
        }

        SpellChangedBlock block = Spells.instance().block(event.getIgnitingBlock().getLocation());
        if(block != null && block.recent() != null && block.recent().spelltype() == SpellType.MOLTEN_TERRAIN) {
            event.setCancelled(true);
        }
    }
}
