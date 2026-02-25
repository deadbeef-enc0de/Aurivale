package net.mctitan.rpg.spell;

import net.mctitan.data.BaseData;
import net.mctitan.data.BasicData;
import net.mctitan.rpg.enums.SpellType;
import net.mctitan.rpg.spell.instance.SpellInstance;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class SpellChangedBlock extends BaseData {
    private Location location = null;
    private Material original;
    private String datastr = null;
    private HashMap<Integer, ItemStack> inventory = null;
    private LinkedList<BlockChange> changes = new LinkedList<>();

    public SpellChangedBlock() {}

    public SpellChangedBlock(Location location) {
        this.location = location.blockloc();
        this.original = this.location.block().getType();
        this.datastr = this.location.block().getBlockData().getAsString();

        // backup container objects
        Inventory inventory = inventory();
        if(inventory != null) {
            this.inventory = new HashMap<>();
            for(int index = 0; index < inventory.getSize(); ++index) {
                ItemStack stack = inventory.getItem(index);
                if(stack == null) { continue; }
                this.inventory.put(index, stack);
            }
        }
    }

    public Location location() { return location; }
    public Block block() { return location.block(); }
    public Material original() { return original; }
    public BlockChange recent() { return (changes.isEmpty() ? null : changes.getLast()); }

    public Inventory inventory() {
        if(!(block().getState() instanceof Container)) {
            return null;
        }

        Container container = (Container)block().getState();
        Inventory inventory = container.getInventory();
        if(container instanceof Chest) {
            Chest chest = (Chest) container;
            inventory = chest.getBlockInventory();
        }

        return inventory;
    }

    public <T extends BlockData> T blockdata(SpellInstance instance) {
        // go through changes and see if the spell instance has a change
        Iterator<BlockChange> iter = changes.iterator();
        while(iter.hasNext()) {
            BlockChange change = iter.next();
            if(change.instance() != instance.id()) {
                continue;
            }

            BlockData blockdata = change.blockdata();
            return (T)blockdata;
        }

        // the spell instance has not changed the block
        return null;
    }

    public void blockdata(SpellInstance instance, BlockData blockdata) {
        // go through and find change block for instance
        Iterator<BlockChange> iter = changes.iterator();
        while(iter.hasNext()) {
            BlockChange change = iter.next();
            if(change.instance() != instance.id()) {
                continue;
            }

            change.blockdata(blockdata);
        }

        // if we are the most recent change, update the actual block
        boolean change = !changes.isEmpty() && changes.getLast().instance() == instance.id();
        if(change) {
            location.block().setBlockData(blockdata, false);
        }
    }

    public void set(SpellInstance instance, Material newtype) {
        // see if we need to make the block change object
        boolean create = true;
        Iterator<BlockChange> iter = changes.iterator();
        while(iter.hasNext()) {
            BlockChange change = iter.next();
            if(change.instance() == instance.id()) {
                // found a change for the instance, update and don't create new change
                change.material(newtype);
                create = false;
                break;
            }
        }

        // create the block change if needed
        if(create) {
            changes.addLast(new BlockChange(instance, newtype));
        }

        // if instance block change is the most recent, update the world
        if(changes.getLast().instance() == instance.id()) {
            block().setType(newtype, false);
        }
    }

    public void revert(SpellInstance instance) {
        // should we restore the previous change
        boolean dochange = changes.size() > 1 && changes.getLast().instance() == instance.id();

        // remove the instance
        Iterator<BlockChange> iter = changes.iterator();
        while(iter.hasNext()) {
            BlockChange change = iter.next();
            if(change.instance() == instance.id()) {
                iter.remove();
            }
        }

        // if we should change, get the last block and change the type
        if(dochange) {
            block().setType(changes.getLast().material());
        }

        // remove the block from the instance
        instance.removeblock(location);

        // restore original block if changes are empty
        if(changes.isEmpty()) {
            // reset the block data
            BlockData data = Bukkit.getServer().createBlockData(datastr);
            block().setBlockData(data, false);

            // reset inventory if it exists
            if(inventory != null) {
                Inventory inv = inventory();
                for(int index : inventory.keySet()) {
                    inv.setItem(index, inventory.get(index));
                }
            }

            // unregister the block change instance
            Spells.instance().unregister(this);
        }
    }

    public static class BlockChange extends BasicData {
        private int instance;
        private SpellType spelltype;
        private Material material;
        private String datastr;

        public BlockChange() {}

        public BlockChange(SpellInstance instance, Material material) {
            this.instance = instance.id();
            this.spelltype = instance.type();
            this.material = material;
            this.datastr = material.createBlockData().getAsString();
        }

        public int instance() { return instance; }
        public SpellType spelltype() { return spelltype; }
        public Material material() { return material; }

        public BlockData blockdata() { return Bukkit.getServer().createBlockData(datastr); }
        public void blockdata(BlockData blockData) { this.datastr = blockData.getAsString(); }

        public void material(Material material) { this.material = material; }
    }
}
