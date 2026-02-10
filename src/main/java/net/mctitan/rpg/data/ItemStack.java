package net.mctitan.rpg.data;

import io.papermc.paper.block.TileStateInventoryHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.crafting.Craftable;
import net.mctitan.rpg.crafting.Craftables;
import net.mctitan.rpg.data.tables.tagged.TaggedTableSelector;
import net.mctitan.rpg.enums.*;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierConfig;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.Modifiers;
import net.mctitan.rpg.modifier.types.EnchantmentModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.Sort;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

public class ItemStack implements Logger {
    /** Cache of all modifiers on all known items */
    static private final Map<UUID, Map<ModifierSlot, List<Modifier>>> modifiers = new HashMap<>();

    /** Bukkit item stack */
    private org.bukkit.inventory.ItemStack bukkitstack;

    /** Inventory item belongs to */
    private Inventory inventory;

    /** Snapshot tile inventory holder */
    private TileStateInventoryHolder holder;

    /** Inventory slot item belongs to */
    private int slot = -1;

    /** Unique ID of the item stack */
    private transient UUID uuid = null;

    /**
     * @param bukkitstack bukkit item stack
     */
    public ItemStack(org.bukkit.inventory.ItemStack bukkitstack) {
        this.bukkitstack = bukkitstack;
    }

    /**
     * Creates a new underlying bukkit item stack
     * @param type type of item the stack is
     */
    public ItemStack(Material type) {
        this(type, 1);
    }

    /**
     * Creates a new underlying bukkit item stack
     * @param type type of item the stack is
     * @param amount the quantity of the stack
     */
    public ItemStack(Material type, int amount) {
        this(new org.bukkit.inventory.ItemStack(type, amount));
    }

    /**
     * Links this item stack to an item stack actually in an inventory
     * @param inventory inventory the bukkit stack exists in
     * @param slot the slot number the item stack is in
     */
    public ItemStack(org.bukkit.inventory.Inventory inventory, int slot) {
        this(inventory, slot, inventory.getItem(slot));
    }

    /**
     * Links this item stack to an item stack actually in an inventory
     * @param inventory inventory the bukkit stack exists in
     * @param slot the slot number the item stack is in
     * @param bukkitstack bukkit item stack
     */
    public ItemStack(org.bukkit.inventory.Inventory inventory, int slot, org.bukkit.inventory.ItemStack bukkitstack) {
        this(bukkitstack);
        this.inventory = inventory;
        this.slot = slot;
    }

    /**
     * Links this item stack to an item stack in a block tile entity
     * @param holder holds the inventory
     * @param slot the slot number the item stack is in
     */
    public ItemStack(TileStateInventoryHolder holder, int slot) {
        this(holder, slot, holder.getSnapshotInventory().getItem(slot));
    }

    /**
     * Links this item stack to an item stack in a block tile entity
     * @param holder holds the inventory
     * @param slot the slot number the item stack is in
     * @param bukkitstack bukkit item stack
     */
    public ItemStack(TileStateInventoryHolder holder, int slot, org.bukkit.inventory.ItemStack bukkitstack) {
        this(bukkitstack);
        this.holder = holder;
        this.slot = slot;
    }

    /**
     * @return copy of the item stack, gets a new uuid if one is set
     */
    public ItemStack clone() {
        ItemStack ret = new ItemStack(this.bukkitstack.clone());
        if(ret.uuid() != null) {
            ret.removeuuid();
            ret.setuuid();
        }

        return ret;
    }

    /**
     * @return gets the inventory this item belongs to
     */
    public Inventory inventory() {
        if(holder != null) { return holder.getSnapshotInventory(); }
        return inventory;
    }

    /**
     * @return gets inventory for a block tile to update its inventory
     */
    public TileStateInventoryHolder holder() { return holder; }

    /**
     * @return gets the inventory slot number this item belongs to
     */
    public int slot() { return slot; }

    /**
     * Gets the underlying bukkit item stack this object represents
     * @return bukkit item stack
     */
    public org.bukkit.inventory.ItemStack bukkitstack() { return bukkitstack; }

    public Integer damage() {
        if(this.bukkitstack == null) {
            return null;
        }

        ItemMeta meta = this.bukkitstack.getItemMeta();
        if(!(meta instanceof Damageable damagemeta)) {
            return null;
        }

        return damagemeta.getDamage();
    }

    /**
     * Deals 1 point of durability damage to the item taking unbreaking into account
     * @return true if the weapon was destroyed, false otherwise
     */
    public boolean adddamage() { return adddamage(1); }

    /**
     * Deals an amount of durability damage to the item taking unbreaking into account
     * @param damage amount of durability damage to deal to the item
     * @return true if the weapon was destroyed, false otherwise
     */
    public boolean adddamage(int damage) {
        boolean ret = false;
        if(this.bukkitstack == null || !(this.bukkitstack.getItemMeta() instanceof Damageable damagemeta)) {
            return ret;
        }

        int unbreaking = enchantment(Enchantment.unbreaking);
        int actualdamage = 0;
        for(int d = 0; d < damage; ++d) {
            if(Math.nextInt(unbreaking + 1) != 0) { continue; }
            ++actualdamage;
        }
        damagemeta.setDamage(damagemeta.getDamage() + actualdamage);
        ret = damagemeta.getDamage() == bukkitstack.getType().getMaxDurability();

        this.bukkitstack.setItemMeta(damagemeta);
        return ret;
    }

    /***** UUID *****/

    /**
     * Gets the uuid of the item stack
     * @return UUID of the item, loads it from the itemstack if need be
     */
    public UUID uuid() {
        if(this.uuid != null) {
            return this.uuid;
        }

        if(this.bukkitstack == null) {
            return null;
        }

        ItemMeta meta = this.bukkitstack.getItemMeta();
        if(meta == null) {
            return null;
        }

        PersistentDataContainer root = meta.getPersistentDataContainer();
        PersistentDataContainer uuidpdc = root.get(Key.UUID_KEY, PersistentDataType.TAG_CONTAINER);
        if(uuidpdc == null) {
            return null;
        }

        long lower = uuidpdc.get(Key.UUID_LOWER_KEY, PersistentDataType.LONG);
        long upper = uuidpdc.get(Key.UUID_UPPER_KEY, PersistentDataType.LONG);
        this.uuid = new UUID(upper, lower);

        return this.uuid;
    }

    /**
     * Sets the uuid on an item stack using a random one
     */
    public void setuuid() { uuid(UUID.randomUUID()); }

    /**
     * Sets the uuid of the item stack using the given one
     * @param uuid the uuid to set on the item stack
     */
    public void uuid(UUID uuid) {
        if(this.bukkitstack == null) {
            return;
        }

        if(uuid() != null) {
            return;
        }

        ItemMeta meta = this.bukkitstack.getItemMeta();
        PersistentDataContainer root = meta.getPersistentDataContainer();
        PersistentDataContainer uuidpdc = root.getAdapterContext().newPersistentDataContainer();
        uuidpdc.set(Key.UUID_LOWER_KEY, PersistentDataType.LONG, uuid.getLeastSignificantBits());
        uuidpdc.set(Key.UUID_UPPER_KEY, PersistentDataType.LONG, uuid.getMostSignificantBits());
        root.set(Key.UUID_KEY, PersistentDataType.TAG_CONTAINER, uuidpdc);

        this.bukkitstack.setItemMeta(meta);
        this.uuid = uuid;
    }

    /** Removes the UUID from the item */
    public void removeuuid() {
        if(this.bukkitstack == null) {
            return;
        }

        synchronized (modifiers) {
            modifiers.remove(uuid());
        }

        ItemMeta meta = this.bukkitstack.getItemMeta();
        meta.getPersistentDataContainer().remove(Key.UUID_KEY);

        this.bukkitstack.setItemMeta(meta);
        this.uuid = null;
    }

    /***** LORE *****/

    /** regenerates the lore for an item */
    private void generatelore() {
        List<Component> lorelines = new ArrayList<>();
        PersistentDataContainer root = this.bukkitstack.getItemMeta().getPersistentDataContainer();
        PersistentDataContainer lorepdc = root.get(Key.LORE_KEY, PersistentDataType.TAG_CONTAINER);

        // set header lore lines
        if(lorepdc != null && lorepdc.has(Key.LORE_HEADER_KEY, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer loreheaderpdc =  lorepdc.get(Key.LORE_HEADER_KEY, PersistentDataType.TAG_CONTAINER);
            for(NamespacedKey key : Sort.slot(loreheaderpdc.getKeys())) {
                String linestr = loreheaderpdc.get(key, PersistentDataType.STRING);
                Component line = MiniMessage.miniMessage().deserialize(linestr);
                lorelines.add(line);
            }
        }

        // go through slots and add modifier then lore
        for(ModifierSlot slot : ModifierSlot.values()) {
            // go through modifiers in this slot
            for(Modifier modifier : modifiers(slot)) {
                if(modifier.showtext()) {
                    lorelines.add(modifier.text().color(slot.color()).decoration(TextDecoration.ITALIC, false));
                }
            }

            // go through set lore for this slot
            if(lorepdc != null && lorepdc.has(slot.lore().key(), PersistentDataType.TAG_CONTAINER)) {
                PersistentDataContainer loreslotpdc = lorepdc.get(slot.lore().key(), PersistentDataType.TAG_CONTAINER);
                for(NamespacedKey key : Sort.slot(loreslotpdc.getKeys())) {
                    String linestr = loreslotpdc.get(key, PersistentDataType.STRING);
                    Component line = MiniMessage.miniMessage().deserialize(linestr);
                    lorelines.add(line);
                }
            }
        }

        // set footer lore lines
        if(lorepdc != null && lorepdc.has(Key.LORE_FOOTER_KEY, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer loreheaderpdc =  lorepdc.get(Key.LORE_FOOTER_KEY, PersistentDataType.TAG_CONTAINER);
            for(NamespacedKey key : Sort.slot(loreheaderpdc.getKeys())) {
                String linestr = loreheaderpdc.get(key, PersistentDataType.STRING);
                Component line = MiniMessage.miniMessage().deserialize(linestr);
                lorelines.add(line);
            }
        }

        this.bukkitstack.lore(lorelines);
    }

    /**
     * Adds a lore line to either pre modifier or post modifier
     * @param loreslot lore slot to add line to
     * @param line the component lore line to add
     */
    public void addlore(LoreSlot loreslot, Component line) {
        ItemMeta meta = this.bukkitstack.getItemMeta();
        PersistentDataContainer root = meta.getPersistentDataContainer();
        PersistentDataContainer lorepdc = root.get(Key.LORE_KEY, PersistentDataType.TAG_CONTAINER);
        if(lorepdc == null) {
            lorepdc = root.getAdapterContext().newPersistentDataContainer();
        }
        PersistentDataContainer lorepartpdc = lorepdc.get(loreslot.key(), PersistentDataType.TAG_CONTAINER);
        if(lorepartpdc == null) {
            lorepartpdc = root.getAdapterContext().newPersistentDataContainer();
        }
        String linestr = MiniMessage.miniMessage().serialize(line);
        lorepartpdc.set(key(lorepartpdc.getKeys().size() + 1), PersistentDataType.STRING, linestr);
        lorepdc.set(loreslot.key(), PersistentDataType.TAG_CONTAINER, lorepartpdc);
        root.set(Key.LORE_KEY, PersistentDataType.TAG_CONTAINER, lorepdc);
        this.bukkitstack.setItemMeta(meta);

        generatelore();
    }

    /***** ATTRIBUTES *****/

    /**
     * Adds a Minecraft attribute to the item
     * @param attribute which attribute type to aadd
     * @param operation how the value increases/decreases the attribute
     * @param key unique key to identify the attribute change
     * @param value value of the change to be done
     * @param slot which items slots this attribute applies to
     */
    public void addattribute(Attribute attribute, AttributeModifier.Operation operation, NamespacedKey key, double value, EquipmentSlot slot) {
        if(this.bukkitstack == null) {
            return;
        }

        AttributeModifier modifier = new AttributeModifier(key, value, operation, slot.getGroup());
        ItemMeta meta = this.bukkitstack.getItemMeta();
        meta.addAttributeModifier(attribute, modifier);
        this.bukkitstack.setItemMeta(meta);
    }

    /***** DISPLAY NAME *****/

    /**
     * @return If this item has a displayname set already
     */
    public boolean hasdisplayname() {
        if(this.bukkitstack == null) {
            return false;
        }

        return this.bukkitstack.getItemMeta().hasDisplayName();
    }

    /**
     * @return Item's set displayname
     */
    public Component displayname() {
        if(this.bukkitstack == null) {
            return Component.text("");
        }

        ItemMeta meta = this.bukkitstack.getItemMeta();
        return meta.displayName();
    }

    /**
     * Sets the item's display name
     * @param displayname display name to set on the item
     */
    public void displayname(Component displayname) {
        if(this.bukkitstack == null) {
            return;
        }

        ItemMeta meta = this.bukkitstack.getItemMeta();
        meta.displayName(displayname.decoration(TextDecoration.ITALIC, false));
        this.bukkitstack.setItemMeta(meta);
    }

    /***** ENCHANTMENTS *****/

    /**
     * Gets the level of enchantment on the item
     * @param enchantment what enchantment to query for
     * @return level of enchantment or 0 if not on the item
     */
    public int enchantment(Enchantment enchantment) {
        if(enchantment.bukkit() != null) {
            if(this.bukkitstack == null || !this.bukkitstack.containsEnchantment(enchantment.bukkit())) {
                return 0;
            }
            return this.bukkitstack.getEnchantmentLevel(enchantment.bukkit());
        }

        int level = 0;
        for(Modifier modifier : modifiers()) {
            if(!(modifier instanceof EnchantmentModifier enchantmod) ||
                    enchantmod.enchantment() != enchantment ||
                    enchantmod.value() <= level) {
                continue;
            }
            level = (int)enchantmod.value();
        }

        return level;
    }

    /**
     * Adds the given enchantment to the item
     * @param enchantment which enchantment to add
     */
    public void addenchantment(Enchantment enchantment) { addenchantment(enchantment, 1); }

    /**
     * Adds the given enchantment to the item
     * @param enchantment which enchantment to add
     * @param level level of enchantment to add
     */
    public void addenchantment(Enchantment enchantment, int level) {
        if(this.bukkitstack == null) {
            return;
        }

        this.bukkitstack.addUnsafeEnchantment(enchantment.bukkit(), level);
        if(enchantment == Enchantment.mending) {
            flag(ItemFlag.HIDE_ENCHANTS, true);
        }
    }

    /**
     * Removes the given enchantment from the item
     * @param enchantment which enchantment to remove
     */
    public void removeenchantment(Enchantment enchantment) {
        if(this.bukkitstack == null) {
            return;
        }

        this.bukkitstack.removeEnchantment(enchantment.bukkit());
        if(!this.bukkitstack.containsEnchantment(Enchantment.mending.bukkit())) {
            flag(ItemFlag.HIDE_ENCHANTS, false);
        }
    }

    /***** FLAGS *****/

    /**
     * Gets the state of an item flag
     * @param flag which flag to interrogate
     * @return true if the flag is set, false otherwise
     */
    public boolean flag(ItemFlag flag) {
        if(this.bukkitstack == null) {
            return false;
        }

        return this.bukkitstack.hasItemFlag(flag);
    }

    /**
     * Sets the state of an item flag
     * @param flag which flag to either set/unset
     * @param set sets if true, unsets otherwise
     */
    public void flag(ItemFlag flag, boolean set) {
        if(this.bukkitstack == null) {
            return;
        }

        if(set) {
            this.bukkitstack.addItemFlags(flag);
        } else {
            this.bukkitstack.removeItemFlags(flag);
        }
    }

    /***** ITEM TYPE *****/

    /**
     * Gets the type of item
     * @return Item type
     */
    public ItemType type() {
        if(this.bukkitstack == null) {
            return null;
        }

        ItemMeta meta = this.bukkitstack.getItemMeta();
        PersistentDataContainer root = meta.getPersistentDataContainer();

        if(!root.has(Key.ITEM_TYPE_KEY, PersistentDataType.STRING)) {
            return null;
        }

        String typestr = root.get(Key.ITEM_TYPE_KEY, PersistentDataType.STRING);
        return ItemType.valueOf(typestr);
    }

    /**
     * Sets the item type
     * @param type type of item
     */
    public void type(ItemType type) {
        if(this.bukkitstack == null) {
            return;
        }

        // set data on item
        ItemMeta meta = this.bukkitstack.getItemMeta();
        PersistentDataContainer root = meta.getPersistentDataContainer();
        root.set(Key.ITEM_TYPE_KEY, PersistentDataType.STRING, type.name());
        this.bukkitstack.setItemMeta(meta);

        // if craftable, set display name
        if(craftable() != null) {
            Component displayname = Component.text(craftable().displayname()).color(type.color());
            displayname(displayname);
        }
    }

    /***** MODIFIERS *****/

    /**
     * Get all modifiers on the item
     * @return modifiers on the item
     */
    public ArrayList<Modifier> modifiers() {
        return modifiers(ModifierSlot.values());
    }

    /**
     * Gets all modifiers in the given slots
     * @param slots lsit of slots to check
     * @return modifiers that are in the given slots
     */
    public ArrayList<Modifier> modifiers(ModifierSlot... slots) {
        ArrayList<Modifier> modifiers = new ArrayList<>();
        for(ModifierSlot slot : slots) {
            modifiers.addAll(modifiers(slot));
        }

        return modifiers;
    }

    /**
     * Get all modifiers in a given slot type
     * @param slot modifier slot type to query
     * @return a copy of the list of modifiers for the slot if it exists, empty list otherwise
     */
    public List<Modifier> modifiers(ModifierSlot slot) {
        // if there is no uuid, there are no modifiers
        if(uuid() == null) {
            return List.of();
        }

        // check to make sure the item is in the cache and the slot has modifiers
        synchronized (modifiers) {
            if(modifiers.containsKey(uuid())) {
                if(modifiers.get(uuid()).containsKey(slot)) {
                    return new ArrayList<>(modifiers.get(uuid()).get(slot));
                } else {
                    return List.of();
                }
            }
        }

        // return a cloned version of the list of modifiers
        List<Modifier> mods = loadmodifiers().get(slot);
        if(mods == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(loadmodifiers().get(slot));
    }

    /**
     * Gets a modifier by id that is on the item stack
     * @param id identifier for the modifier
     * @return modifier if found on the item stack, null otherwise
     */
    public Modifier modifier(String id) { return modifier(id, ModifierSlot.values()); }

    /**
     * Gets a modifier by id in one of the given slots on the item stack
     * @param id identifier for the modifier
     * @param slots which slots to search
     * @return modifier if found in one of the slots on the item stack, null otherwise
     */
    public Modifier modifier(String id, ModifierSlot... slots) {
        for(Modifier modifier : modifiers(slots)) {
            if(modifier.id().equals(id)) {
                return modifier;
            }
        }
        return null;
    }

    /**
     * Whether the item has the specified modifier on it
     * @param id modifier id to check
     * @return true if the item has the modifier, false otherwise
     */
    public boolean hasmodifier(String id) {
        return hasmodifier(id, ModifierSlot.values());
    }

    /**
     * Whether the item has the specified modifier in the slots given
     * @param id modifier id to check
     * @param slots slots to check for the modifier
     * @return true if the item as the modifier in one of the slots, false otherwise
     */
    public boolean hasmodifier(String id, ModifierSlot... slots) {
        List<Modifier> modifiers = modifiers(slots);
        for(Modifier modifier : modifiers) {
            if(modifier.id().equals(id)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Load the modifiers on the item stack and return them
     * @return modifiers that were loaded from the item
     */
    private Map<ModifierSlot, List<Modifier>> loadmodifiers() {
        // if there is no uuid, there are no modifiers
        if(uuid() == null) {
            return Map.of();
        }
        log(Level.INFO, "Loading modifiers for uuid="+uuid());

        Map<ModifierSlot, List<Modifier>> ret = new HashMap<>();
        PersistentDataContainer root = this.bukkitstack.getItemMeta().getPersistentDataContainer();

        // if we don't even have a modifiers tag, there is nothing to do
        if(!root.has(Key.MODIFIERS_KEY, PersistentDataType.TAG_CONTAINER)) {
            return ret;
        }
        PersistentDataContainer modifierspdc = root.get(Key.MODIFIERS_KEY, PersistentDataType.TAG_CONTAINER);

        // go through each slot and load the modifier
        for(ModifierSlot slot : ModifierSlot.values()) {
            if(!modifierspdc.has(slot.key(), PersistentDataType.TAG_CONTAINER)) {
                continue;
            }
            PersistentDataContainer slotpdc = modifierspdc.get(slot.key(), PersistentDataType.TAG_CONTAINER);

            // each entry is a modifier
            for(NamespacedKey key : Sort.slot(slotpdc.getKeys())) {
                PersistentDataContainer modifierpdc = slotpdc.get(key, PersistentDataType.TAG_CONTAINER);
                String classname = modifierpdc.get(Key.CLASS_KEY, PersistentDataType.STRING);

                // get the class for the modifier
                Class<? extends Modifier> clazz = null;
                try {
                    clazz = Class.forName(classname).asSubclass(Modifier.class);
                } catch(ClassNotFoundException e) {
                    log(Level.SEVERE, "Modifier not found classname="+classname, e);
                    continue;
                }

                // get the constructor for the modifier
                Constructor<? extends Modifier> constructor = null;
                try {
                    constructor = clazz.getConstructor(PersistentDataContainer.class);
                } catch(NoSuchMethodException e) {
                    log(Level.SEVERE, "PDC constructor not found for classname="+classname, e);
                    continue;
                }

                // get the modifier
                Modifier modifier = null;
                try {
                    modifier = constructor.newInstance(modifierpdc);
                } catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    log(Level.SEVERE, "Construction failure for classname="+classname, e);
                    continue;
                }

                // add the modifier
                if(!ret.containsKey(slot)) {
                    ret.put(slot, new ArrayList<>());
                }
                ret.get(slot).add(modifier);
            }
        }

        // update item cache and return modifiers
        if(!ret.isEmpty()) {
            synchronized (modifiers) {
                modifiers.put(uuid(), ret);
            }
        }
        return ret;
    }

    /**
     * Adds modifier to the item using the default slot from the ModifierConfig
     * @param modifier the modifier to add
     */
    public void addmodifier(Modifier modifier) { addmodifier(modifier.config().slot(), modifier); }

    /**
     * Adds modifier to the item
     * @param slot which kind of slot to add the modifier to
     * @param modifier the modifier to add
     */
    public void addmodifier(ModifierSlot slot, Modifier modifier) {
        // if the item is ephemeral remove uuid
        if(this.craftable() != null && this.craftable().ephemeral()) {
            removeuuid();
        }

        // if there is no uuid, we should make one
        if(uuid() == null) {
            setuuid();
        }

        // add modifier to persistent data
        ItemMeta meta = this.bukkitstack.getItemMeta();
        PersistentDataContainer root = meta.getPersistentDataContainer();
        if(!root.has(Key.MODIFIERS_KEY, PersistentDataType.TAG_CONTAINER)) {
            root.set(Key.MODIFIERS_KEY, PersistentDataType.TAG_CONTAINER, root.getAdapterContext().newPersistentDataContainer());
        }

        PersistentDataContainer modifierspdc = root.get(Key.MODIFIERS_KEY, PersistentDataType.TAG_CONTAINER);
        if(!modifierspdc.has(slot.key(), PersistentDataType.TAG_CONTAINER)) {
            modifierspdc.set(slot.key(), PersistentDataType.TAG_CONTAINER, root.getAdapterContext().newPersistentDataContainer());
        }

        PersistentDataContainer slotpdc = modifierspdc.get(slot.key(), PersistentDataType.TAG_CONTAINER);
        NamespacedKey modifierkey = key(slotpdc.getKeys().size() + 1);
        PersistentDataContainer modifierpdc = slotpdc.getAdapterContext().newPersistentDataContainer();
        modifier.save(modifierpdc);
        slotpdc.set(modifierkey, PersistentDataType.TAG_CONTAINER, modifierpdc);
        modifierspdc.set(slot.key(), PersistentDataType.TAG_CONTAINER, slotpdc);
        root.set(Key.MODIFIERS_KEY, PersistentDataType.TAG_CONTAINER, modifierspdc);
        this.bukkitstack.setItemMeta(meta);

        // add modifier to cache for this item
        synchronized (modifiers) {
            if(modifiers.containsKey(uuid())) {
                if(!modifiers.get(uuid()).containsKey(slot)) { modifiers.get(uuid()).put(slot, new ArrayList<>()); }
                modifiers.get(uuid()).get(slot).add(modifier);
            } else {
                loadmodifiers();
            }
        }

        // regenerate lore lines
        generatelore();
    }

    /**
     * Adds a number of modifier to the item from the craftable magic modifier config
     * @param count number of modifiers to add
     */
    public void addmodifiers(Random random, int count) {
        addmodifiers(random, count, 0);
    }

    /**
     * Adds a number of modifier to the item from the craftable magic modifier config
     * @param count number of modifiers to add
     * @param skill skill check for adding modifiers
     */
    public void addmodifiers(Random random, int count, int skill) {
        addmodifiers(new TaggedTableSelector(), random, count, skill);
    }

    /**
     * Adds a number of modifier to the item from the craftable magic modifier config
     * @param selector tagged entry selector that changes how modifiers are chosen
     * @param count number of modifiers to add
     * @param skill skill check for adding modifiers
     */
    public void addmodifiers(TaggedTableSelector selector, Random random, int count, int skill) {
        // if this item stack isn't a craftable, bail
        Craftable craftable = this.craftable();
        if(craftable == null) {
            return;
        }

        // clone the selector and all existing modifier ids to the black list
        selector = selector.clone();
        for(Modifier modifier : modifiers()) {
            selector.blacklist(modifier.id());
        }

        for(int i = 0; i < count; ++i) {
            // blacklist prefixes and suffixes if there are enough of those slots filled
            if(modifiers(ModifierSlot.PREFIX).size() >= type().prefixes()) {
                selector.blacklist(ModifierSlot.PREFIX.keyname());
            }
            if(modifiers(ModifierSlot.SUFFIX).size() >= type().suffixes()) {
                selector.blacklist(ModifierSlot.SUFFIX.keyname());
            }

            // get the modifier
            ModifierTemplate template = craftable.magicmodifiers().subset(selector).get(random, skill);
            if(template == null) {
                log(Level.SEVERE, "Could not add modifier!");
                continue;
            }
            Modifier modifier = template.modifier(random);

            // add modifier to the item
            try {
                modifier.apply(this);
            } catch(Exception e) {
                log(Level.SEVERE, String.format("Could not apply modifier="+modifier.id()), e);
            }

            // update selector to remove added modifier
            selector.blacklist(modifier.id());
        }
    }

    /**
     * Attempts to add a forced modifier then a number of modifiers -1 from the craftable magic modifier config
     * @param modifierconfig forced modifier that will be attempted to be added
     * @param count number of modifiers to add
     */
    public void addmodifiers(ModifierConfig modifierconfig, Random random, int count) {
        addmodifiers(modifierconfig, random, count, 0);
    }

    /**
     * Attempts to add a forced modifier then a number of modifiers -1 from the craftable magic modifier config
     * @param modifierconfig forced modifier that will be attempted to be added
     * @param count number of modifiers to add
     * @param skill skill check for adding modifiers
     */
    public void addmodifiers(ModifierConfig modifierconfig, Random random, int count, int skill) {
        addmodifiers(modifierconfig, new TaggedTableSelector(), random, count, skill);
    }

    /**
     * Attempts to add a forced modifier then a number of modifiers -1 from the craftable magic modifier config
     * @param modifierconfig forced modifier that will be attempted to be added
     * @param selector tagged entry selector that changes how modifiers are chosen
     * @param count number of modifiers to add
     * @param skill skill check for adding modifiers
     */
    public void addmodifiers(ModifierConfig modifierconfig, TaggedTableSelector selector, Random random, int count, int skill) {
        // if this item stack isn't a craftable, bail
        Craftable craftable = this.craftable();
        if(craftable == null) {
            return;
        }

        // add the required modifier unless the craftable cannot roll it or if the modifier already is on it or the selector bars it
        if(modifierconfig != null && craftable.maigcmodifierkeys().contains(modifierconfig.id()) &&
                !hasmodifier(modifierconfig.id()) && selector.matches(modifierconfig.tags())) {
            ModifierTemplate template = modifierconfig.table().get(random, skill);
            Modifier modifier = template.modifier();
            modifier.apply(this);
            --count;
        }

        // if there are more modifiers left, add them
        if(count > 0) {
            addmodifiers(selector, random, count, skill);
        }
    }

    /**
     * Removes all modifiers of the given slot
     * @param slot what modifier slot to remove all modifiers from
     */
    public void removemodifiers(ModifierSlot slot) {
        for(Modifier modifier : modifiers(slot)) {
            removemodifier(slot, modifier);
        }
    }

    /**
     * Removes the given modifier from a slot adjusting all keys to be numerically correct
     * does nothing if the given slot does not have the modifier given
     * @param slot which kind of slow to remove the modifier from
     * @param modifier the modifier to remove
     */
    public void removemodifier(ModifierSlot slot, Modifier modifier) {
        // if the item is ephemeral regen the uuid
        if(this.craftable() != null && this.craftable().ephemeral()) {
            removeuuid();
            setuuid();
        }

        // get the slot data
        String modifierstr = PlainTextComponentSerializer.plainText().serialize(modifier.text());
        ItemMeta meta = this.bukkitstack.getItemMeta();
        PersistentDataContainer root = meta.getPersistentDataContainer();
        if(!root.has(Key.MODIFIERS_KEY, PersistentDataType.TAG_CONTAINER)) {
            log(Level.WARNING, "Modifiers data missing from item, cannot remove slot="+slot+" modifier="+modifierstr);
            return;
        }
        PersistentDataContainer modifierspdc = root.get(Key.MODIFIERS_KEY, PersistentDataType.TAG_CONTAINER);
        if(!modifierspdc.has(slot.key(), PersistentDataType.TAG_CONTAINER)) {
            log(Level.WARNING, "Not modifiers in slot="+slot+" cannot remove modifier="+modifierstr);
            return;
        }
        PersistentDataContainer slotpdc = modifierspdc.get(slot.key(), PersistentDataType.TAG_CONTAINER);

        // make a new slot pdc, copy over kept modifiers, put the data onto the item
        PersistentDataContainer newslotpdc = root.getAdapterContext().newPersistentDataContainer();
        for(NamespacedKey key : Sort.slot(slotpdc.getKeys())) {
            PersistentDataContainer modifierpdc = slotpdc.get(key, PersistentDataType.TAG_CONTAINER);
            PersistentDataContainer templatepdc = modifierpdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER);
            String id = templatepdc.get(Key.ID_KEY, PersistentDataType.STRING);
            if(!id.equals(modifier.id())) {
                newslotpdc.set(key(newslotpdc.getKeys().size() + 1), PersistentDataType.TAG_CONTAINER, modifierpdc);
            }
        }

        // check to see if a modifier was actually removed, if not log issue and do nothing else
        if(slotpdc.getKeys().size() == newslotpdc.getKeys().size()) {
            log(Level.WARNING, "From slot="+slot+" did not remove modifier="+modifierstr);
            return;
        }

        // remove the slot entirely if there are no modifiers in the slot
        if(newslotpdc.isEmpty()) {
            modifierspdc.remove(slot.key());
        } else {
            modifierspdc.set(slot.key(), PersistentDataType.TAG_CONTAINER, newslotpdc);
        }

        // remove modifiers tag if there are no modifiers on the item
        if(modifierspdc.isEmpty()) {
            root.remove(Key.MODIFIERS_KEY);
        } else {
            root.set(Key.MODIFIERS_KEY, PersistentDataType.TAG_CONTAINER, modifierspdc);
        }
        this.bukkitstack.setItemMeta(meta);

        // remove modifier from cache for this item
        synchronized (modifiers) {
            if(modifiers.containsKey(uuid())) {
                modifiers.get(uuid()).get(slot).remove(modifier);
                if(modifiers.get(uuid()).get(slot).isEmpty()) {
                    modifiers.get(uuid()).remove(slot);
                }
            } else {
                loadmodifiers();
            }
        }

        // if there are no modifiers, remove the uuid
        if(modifiers().isEmpty()) {
            removeuuid();
        }

        // regenerate lore lines
        generatelore();
    }

    /***** SIMPLE PERSISTENT *****/

    /**
     * Checks if the item has the key set on the item
     * @param key unique id of the data
     * @return true if the item has the data set, false otherwise
     */
    public boolean has(NamespacedKey key) {
        return has(key, PersistentDataType.BOOLEAN);
    }

    /**
     * Checks if the item has the key set on the item
     * @param key unique id of the data
     * @param type type of data to check
     * @return true if the item has the data set, false otherwise
     */
    public boolean has(NamespacedKey key, PersistentDataType type) {
        if(this.bukkitstack == null) {
            return false;
        }

        ItemMeta meta = this.bukkitstack.getItemMeta();
        if(meta == null) {
            return false;
        }

        return meta.getPersistentDataContainer().has(key, type);
    }

    /**
     * Gets the boolean root data from persistence referenced by the given key
     * @param key unique id of the data
     * @return boolean value stored on the key
     */
    public boolean get(NamespacedKey key) {
        return get(key, PersistentDataType.BOOLEAN);
    }

    /**
     * Gets root level data from persistence referenced by the key
     * @param key unique id of the data
     * @param type type of data to return
     * @return data found at the key if found and the correct type, null otherwise
     * @param <T> object type of the return
     */
    public <T> T get(NamespacedKey key, PersistentDataType type) {
        return get(key, type, null);
    }

    /**
     * Gets root level data from persistence referenced by the key
     * @param key unique id of the data
     * @param type type of data to return
     * @param defaultvalue value to return if not found on item
     * @return data found at the key if found and the correct type, null otherwise
     * @param <T> object type of the return
     */
    public <T> T get(NamespacedKey key, PersistentDataType type, T defaultvalue) {
        if(this.bukkitstack == null) {
            return defaultvalue;
        }

        ItemMeta meta = this.bukkitstack.getItemMeta();
        if(meta == null) {
            return defaultvalue;
        }

        PersistentDataContainer root = meta.getPersistentDataContainer();
        if(!root.has(key, type)) {
            return defaultvalue;
        }

        return (T)root.get(key, type);
    }

    /**
     * Sets root level data at the given key
     * @param key unique id of the data
     * @param value data to be saved
     */
    public void set(NamespacedKey key, boolean value) {
        set(key, PersistentDataType.BOOLEAN, value);
    }

    /**
     * Sets root level data at the given key
     * @param key unique id of the data
     * @param type type of data that is being saved
     * @param value data to be saved
     * @param <T> object type of the data
     */
    public <T> void set(NamespacedKey key, PersistentDataType type, T value) {
        if(this.bukkitstack == null) {
            return;
        }

        ItemMeta meta = this.bukkitstack.getItemMeta();
        PersistentDataContainer root = meta.getPersistentDataContainer();
        root.set(key, type, value);
        this.bukkitstack.setItemMeta(meta);
    }

    /***** CRAFTABLE *****/

    /**
     * @return Gets The craftable represented by the item if it is one, null otherewise
     */
    public Craftable craftable() {
        String keyname = get(Key.CRAFTABLE_KEY, PersistentDataType.STRING);
        if(keyname == null) {
            return null;
        }
        return Craftables.instance().craftable(keyname);
    }

    /**
     * Sets teh craftable on the item
     * @param craftable the craftable to set on the item
     */
    public void craftable(Craftable craftable) {
        set(Key.CRAFTABLE_KEY, PersistentDataType.STRING, craftable.key().getKey());
    }

    /***** ENCHANTING *****/

    /**
     * @return Gets the enchantability of the item
     */
    public boolean enchantable() {
        if(has(Key.ENCHANTABLE_KEY)) {
            return get(Key.ENCHANTABLE_KEY);
        }
        return false;
    }

    /**
     * Sets the enchantability of the item
     * @param enchantable true if the item is enchantable, false otherwise
     */
    public void enchantable(boolean enchantable) {
        set(Key.ENCHANTABLE_KEY, enchantable);
    }

    /**
     * Gets the enchanter type of the stack
     * @return enchanter type if the stack is an enchanter item, null otherwise
     */
    public EnchanterType enchantertype() {
        String enchantertypestr = get(Key.ENCHANTER_KEY, PersistentDataType.STRING);
        if(enchantertypestr != null) {
            return EnchanterType.valueOf(enchantertypestr);
        }

        return null;
    }

    /**
     * Sets the enchanter type of the stack
     * @param enchantertype enchanter type to set the stack to
     */
    public void enchantertype(EnchanterType enchantertype) {
        set(Key.ENCHANTER_KEY, PersistentDataType.STRING, enchantertype.name());
    }

    /**
     * @return sets the enchanter modifier on an item if it exists
     */
    public ModifierConfig enchantermod() {
        String enchantermod = get(Key.ENCHANTER_MOD_KEY, PersistentDataType.STRING);
        return Modifiers.instance().config(enchantermod);
    }

    /**
     * Sets the enchanter modifier on the item
     * @param modifierconfig which modifier to add
     */
    public void enchantermod(ModifierConfig modifierconfig) {
        if(enchantertype() == null) {
            return;
        }

        set(Key.ENCHANTER_MOD_KEY, PersistentDataType.STRING, modifierconfig.id());
        addlore(LoreSlot.IMPLICIT, Component.text(modifierconfig.name())
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false)
        );
        for(Component modhelpline : enchantertype().enchanter().modhelp()) {
            addlore(LoreSlot.IMPLICIT, modhelpline);
        }
    }

    /***** MISC *****/

    /**
     * Gets a persistent data key from a slot number
     * @param slot slot number of the modifier
     * @return persistent data key for the given slot
     */
    private NamespacedKey key(int slot) {
        return new NamespacedKey(Aurivale.instance(), String.valueOf(slot));
    }

    /**
     * Gets the slot number from a persistent data key
     * @param key persistent data key
     * @return slot number for the given persistent data key
     */
    private int slot(NamespacedKey key) {
        String slotstr = key.getKey();
        try {
            return Integer.parseInt(slotstr);
        } catch(NumberFormatException e) {
            log(Level.SEVERE, "Invalid slot="+slotstr, e);
        }
        return -1;
    }

    /**
     * Sets the resource pack model data
     * @param modeldata model data to set
     */
    public void modeldata(int modeldata) {
        if(this.bukkitstack == null) {
            return;
        }

        ItemMeta meta = this.bukkitstack.getItemMeta();
        CustomModelDataComponent customModel = meta.getCustomModelDataComponent();
        customModel.setFloats(List.of((float)modeldata));
        meta.setCustomModelDataComponent(customModel);
        this.bukkitstack.setItemMeta(meta);
    }

    /**
     * @return true if the item is immutable, false otherwise
     */
    public boolean immutable() {
        if(!has(Key.IMMUTABLE_KEY)) {
            return false;
        }
        return get(Key.IMMUTABLE_KEY, PersistentDataType.BOOLEAN);
    }

    /**
     * Makes the item immutable
     */
    public void setimmutable() {
        set(Key.IMMUTABLE_KEY, PersistentDataType.BOOLEAN, true);
        addlore(LoreSlot.UNIQUE, Component.text("Immutable")
                .color(NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false)
        );
    }
}
