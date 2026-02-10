package net.mctitan.rpg;

import net.mctitan.rpg.brewing.BrewingRecipes;
import net.mctitan.rpg.commands.AurivaleCommand;
import net.mctitan.rpg.commands.AurivaleHandbookCommand;
import net.mctitan.rpg.config.Configuration;
import net.mctitan.rpg.crafting.Craftables;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.brewing.BrewingStands;
import net.mctitan.rpg.dimension.Dimensions;
import net.mctitan.rpg.gamerules.Gamerules;
import net.mctitan.rpg.handler.enchantment.*;
import net.mctitan.rpg.handler.general.*;
import net.mctitan.rpg.handler.spell.*;
import net.mctitan.rpg.handler.monster.*;
import net.mctitan.rpg.handler.trigger.*;
import net.mctitan.rpg.loot.LootDrops;
import net.mctitan.rpg.modifier.Modifiers;
import net.mctitan.rpg.monster.Monsters;
import net.mctitan.rpg.pack.PackManager;
import net.mctitan.rpg.pack.PackModifiers;
import net.mctitan.rpg.potion.LingeringPotionClouds;
import net.mctitan.rpg.spell.Spells;
import net.mctitan.rpg.trading.Trading;
import net.mctitan.rpg.unique.Uniques;
import net.mctitan.rpg.util.Version;
import net.mctitan.rpg.visual.Visuals;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Aurivale extends JavaPlugin {
    private static Aurivale instance;
    private Configuration config = null;
    private Version version = null;

    @Override
    public void onEnable() {
        // set the instance of the plugin
        instance = this;

        // load the configurations
        config = new Configuration();
        version = new Version(getConfig("plugin").getString("version"));

        // initialize components
        Gamerules.initialize();
        Modifiers.initialize();
        Craftables.initialize();
        Uniques.initialize();
//        Dimensions.initialize();
        Monsters.initialize();
        LootDrops.initialize();
        Trading.initialize();
        BrewingRecipes.initialize();
        BrewingStands.initialize();
        Spells.initialize();
        PackModifiers.initialize();
        PackManager.initialize();
        Visuals.initialize();
        Parties.initialize();

        // register enchantment handlers
        Bukkit.getPluginManager().registerEvents(EffectArea.instance(), this);
        Bukkit.getPluginManager().registerEvents(ItemBag.instance(), this);
        Bukkit.getPluginManager().registerEvents(ProjectileRider.instance(), this);
        Bukkit.getPluginManager().registerEvents(Smelting.instance(), this);
        Bukkit.getPluginManager().registerEvents(StraightShot.instance(), this);
        Bukkit.getPluginManager().registerEvents(TreeFeller.instance(), this);

        // register general handlers
        Bukkit.getPluginManager().registerEvents(Brewing.instance(), this);
        Bukkit.getPluginManager().registerEvents(Combat.instance(), this);
        Bukkit.getPluginManager().registerEvents(Crafting.instance(), this);
        Bukkit.getPluginManager().registerEvents(DisabledFeatures.instance(), this);
        Bukkit.getPluginManager().registerEvents(Enchanting.instance(), this);
        Bukkit.getPluginManager().registerEvents(EntityDeath.instance(), this);
        Bukkit.getPluginManager().registerEvents(EntityPacks.instance(), this);
        Bukkit.getPluginManager().registerEvents(EnvironmentalDamage.instance(), this);
        Bukkit.getPluginManager().registerEvents(EquipmentChange.instance(), this);
        Bukkit.getPluginManager().registerEvents(HandbookGiver.instance(), this);
        Bukkit.getPluginManager().registerEvents(HealthTracking.instance(), this);
        Bukkit.getPluginManager().registerEvents(ItemCooldown.instance(), this);
        Bukkit.getPluginManager().registerEvents(NoExpDrop.instance(), this);
        Bukkit.getPluginManager().registerEvents(Parties.instance(), this);
        Bukkit.getPluginManager().registerEvents(PlayerChat.instance(), this);
        Bukkit.getPluginManager().registerEvents(PlayerJoin.instance(), this);
        Bukkit.getPluginManager().registerEvents(PlayerRespawn.instance(), this);
        Bukkit.getPluginManager().registerEvents(PlayerStatScoreboard.instance(), this);
        Bukkit.getPluginManager().registerEvents(Potions.instance(), this);
        Bukkit.getPluginManager().registerEvents(RandomLoot.instance(), this);
        Bukkit.getPluginManager().registerEvents(SpellCasting.instance(), this);
        Bukkit.getPluginManager().registerEvents(StatusEffectChanges.instance(), this);
        Bukkit.getPluginManager().registerEvents(VillagerTrade.instance(), this);
        Bukkit.getPluginManager().registerEvents(VisualLoading.instance(), this);

        // register spell handlers
        Bukkit.getPluginManager().registerEvents(FireballSpellHandler.instance(), this);
        Bukkit.getPluginManager().registerEvents(IcePrisonHandler.instance(), this);
        Bukkit.getPluginManager().registerEvents(MoltenTerrainHandler.instance(), this);

        // register monster handlers
        Bukkit.getPluginManager().registerEvents(SlimeHandler.instance(), this);
        Bukkit.getPluginManager().registerEvents(WitchHandler.instance(), this);

        // register trigger handlers
        Bukkit.getPluginManager().registerEvents(ModifierTriggers.instance(), this);

        // setup commands
        this.getCommand("rpg").setExecutor(new AurivaleCommand());
        this.getCommand("help").setExecutor(new AurivaleHandbookCommand());
    }

    @Override
    public void onDisable() {
        // save all entities and players
        DataManager.instance().disable();

        // make sure lingering potion clouds are updated
        LingeringPotionClouds.instance().disable();

        // make sure persist all spell instances and spell blocks
        Spells.instance().disable();

        // unset the instance of the plugin
        instance = null;
    }

    public static Aurivale instance() { return instance; }

    public org.bukkit.configuration.Configuration getConfig(String name) { return config.config(name); }

    public Version version() { return version; }
}
