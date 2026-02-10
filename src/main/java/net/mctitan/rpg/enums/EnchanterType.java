package net.mctitan.rpg.enums;

import net.mctitan.rpg.enchanting.Enchanter;
import net.mctitan.rpg.enchanting.highmagic.*;
import net.mctitan.rpg.enchanting.lowmagic.*;
import net.mctitan.rpg.enchanting.special.*;
import net.mctitan.rpg.util.Text;

public enum EnchanterType {
    // Low Magic Enchanters
    WHISPERING_EMBER    (new WhisperingEmber(), EnchanterFlags.MODABLE),    // Normal -> Magic with 1 prefix 1 suffix
    CHARM_OF_ENRICHMENT (new CharmOfEnrichment(), EnchanterFlags.MODABLE), // Add Modifier to Magic up to 2/2 Modifiers
    FATEBINDERS_PRISM  (new FatebindersPrism(), EnchanterFlags.MODABLE), // Normal -> Magic(4 mod)/Rare(8 mod)/Unique
//    STONE_OF_RENEWAL    (new StoneOfRenewal(), EnchanterFlags.MODABLE),     // Reforge Magic with 2-4 Modifiers

    // High Magic Enchanters
    ASCENDANT_SEAL      (new AscendantSeal(), EnchanterFlags.MODABLE),      // Magic -> Rare Add 1 prefix 1 suffix
    CELESTIAL_CATALYST  (new CelestialCatalyst(), EnchanterFlags.MODABLE),  // Normal -> Rare with 5-6 modifiers 2-3 prefixes 2-3 suffixes
    ECHO_OF_ASCENSION   (new EchoOfAscension(), EnchanterFlags.MODABLE), // Add Modifier to Rare up to 4/4 Modifiers
    VOIDTEAR_SIGIL      (new VoidtearSigil(), EnchanterFlags.MODABLE), // Magic/Rare Remove Modifier
    ORB_OF_FLUX         (new OrbOfFlux(), EnchanterFlags.MODABLE), // Re-roll Modifier Numeric Values
//    ELDRITCH_CRUCIBLE   (new EldritchCrucible(), EnchanterFlags.MODABLE),   // Reforge Rare with 6-8 Modifiers
//    VORTEX_PULSE        (new VortexPulse(), EnchanterFlags.MODABLE), // Magic/Rare remove modifiers change to Normal
//    VEILBORN_CHISEL     (null, EnchanterFlags.MODABLE), // Magic/Rare Remove Modifier Add Modifier

    // Special Enchanters
    PHANTOM_REFLECTION  (new PhantomReflection(), EnchanterFlags.NOT_MODABLE), // Make Immutable Copy of a Magic/Rare
    MARK_OF_ETERNITY    (new MarkOfEternity(), EnchanterFlags.MODABLE), // Rare Item without Implicit moves a Modifier to an Implicit Slot
    SUNDERING_RUNE      (new SunderingRune(), EnchanterFlags.NOT_MODABLE), // Destroy Rare get ESSENCE_SHARD with One Mod Type from Item
    ESSENCE_SHARD       (new EssenceShard(), EnchanterFlags.META), // Add Mod Type to Regular Enchanter
    ;

    private String string = "";
    private Enchanter enchanter;
    private EnchanterFlags flags;

    EnchanterType(Enchanter enchanter, EnchanterFlags flags) {
        this.enchanter = enchanter;
        this.flags = flags;

        // setup camel case name of enchanter
        string = Text.enumtoprint(name());
    }

    public String string() { return string; }
    public Enchanter enchanter() { return enchanter; }
    public boolean modable() { return flags.modable(); }
    public boolean meta() { return flags.meta(); }
}

/*

Transmutes a rare item into another rare item, rerolling all modifiers while keeping the same number of modifiers.
Name: Veilborn Chisel

Transmutes a normal item into a magic item, adding a random modifier based on item type.
Name: Aetherstone Prism

Reforges a magic item to change one modifier to another while keeping the original item’s rarity.
Name: Eclipse Forge

Randomly replaces one modifier on a magic or rare item with a more powerful version.
Name: Winds of Ascendancy

Transfers a single modifier from a magic item to a normal item.
Name: Arcane Transference

Allows the enchantment of a single modifier from an item to another item, keeping the second item unmodified.
Name: Binding Sigil

Temporarily enhances a modifier’s effect on a magic or rare item for a limited time or number of uses.
Name: Tempest Engram

Creates a temporary protective enchantment around a magic or rare item, shielding it from further enchantment for a time.
Name: Veil of Serenity

Changes the elemental nature of a modifier (e.g., fire to ice) while keeping other attributes intact.
Name: Elemental Nexus

Voidflare Nexus
Elder’s Radiance
Stellar Incantation
Arcane Sanctum
Spectral Forge
Twilight Embrace
Astral Beacon
Searing Veil
Netherstone Prism
Omniscient Shard
Silent Omen
Runebound Chalice
Luminous Echelon
Specter’s Lantern
Frostweaver’s Crown
Seraphic Conduit
Dawn’s Wrath
Obsidian Dawn

 */
