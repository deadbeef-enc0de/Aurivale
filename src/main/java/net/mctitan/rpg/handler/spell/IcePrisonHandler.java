package net.mctitan.rpg.handler.spell;

import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.SpellChangedBlock;
import net.mctitan.rpg.spell.Spells;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

public class IcePrisonHandler implements Listener {
    private static final IcePrisonHandler instance = new IcePrisonHandler();

    private IcePrisonHandler() {}

    public static IcePrisonHandler instance() { return instance; }

    @EventHandler
    public void onBlockFace(BlockFadeEvent event) {
        SpellChangedBlock block = Spells.instance().block(event.getBlock().getLocation());
        if(block != null && block.recent() != null && block.recent().spelltype() == SpellType.ICE_PRISON) {
            event.setCancelled(true);
        }
    }
}
