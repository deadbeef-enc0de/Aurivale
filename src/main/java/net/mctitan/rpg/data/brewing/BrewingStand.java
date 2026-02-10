package net.mctitan.rpg.data.brewing;

import net.mctitan.data.BaseData;
import net.mctitan.rpg.brewing.BrewingRecipes;
import net.mctitan.rpg.brewing.BrewingStands;
import net.mctitan.rpg.brewing.recipe.BrewingRecipe;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.BrewingStandSlot;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;

public class BrewingStand extends BaseData {
    private static final int NORMAL_BREWING_TIME = 400;

    private Location location;
    private BrewingRecipeInstance recipeinstance;
    private transient boolean loaded = false;

    public BrewingStand() {}

    public BrewingStand(Location location) {
        this.location = location;
        loaded = true;
    }

    public Location location() { return location; }
    public BrewingRecipeInstance recipeinstance() { return recipeinstance; }
    public boolean loaded() { return loaded; }

    public void loaded(boolean loaded) {
        this.loaded = loaded;
        if(recipeinstance != null) {
            if (loaded) { recipeinstance.start(location); }
            else { recipeinstance.stop(); }
        }
    }

    public void initialize() {
        if(recipeinstance != null) {
            recipeinstance.initialize();
        }
    }

    public void checkfuel() {
        // get bukkit stand and make sure it has fuel to check against
        org.bukkit.block.BrewingStand bukkitstand = (org.bukkit.block.BrewingStand)location.block().getState();
        ItemStack fuelstack = new ItemStack(bukkitstand.getInventory().getFuel());
        if(fuelstack.bukkitstack() == null || fuelstack.bukkitstack().getType() == Material.BLAZE_POWDER) {
            return;
        }

        checkfuel(bukkitstand);
    }

    public void checkfuel(org.bukkit.block.BrewingStand bukkitstand) {
        // get fuel stack
        ItemStack fuelstack = new ItemStack(bukkitstand.getInventory().getFuel());

        // figure out how much fuel needs to be burned
        int missingfuel = 20 - bukkitstand.getFuelLevel();
        int fuelcrafts = BrewingRecipes.instance().fuel(fuelstack.bukkitstack().getType());
        int neededfuel = missingfuel / fuelcrafts;
        int fuelused = Math.min(neededfuel, fuelstack.bukkitstack().getAmount());

        // remove fuel from slot and add fuel to internal buffer
        bukkitstand.getSnapshotInventory().setFuel(fuelstack.bukkitstack().asQuantity(fuelstack.bukkitstack().getAmount() - fuelused));
        bukkitstand.setFuelLevel(bukkitstand.getFuelLevel() + fuelcrafts * fuelused);
        bukkitstand.update();
    }

    public void checkrecipe(Player player) {
        if(recipeinstance != null) {
            return;
        }

        // get bukkit stand to access inventory and block data
        org.bukkit.block.BrewingStand bukkitstand = (org.bukkit.block.BrewingStand)location.block().getState();

        // get ingredient and potions
        ItemStack ingredient = new ItemStack(bukkitstand.getSnapshotInventory().getIngredient());
        ItemStack leftpotion = new ItemStack(bukkitstand, BrewingStandSlot.POTION_LEFT.index());
        ItemStack middlepotion = new ItemStack(bukkitstand, BrewingStandSlot.POTION_MIDDLE.index());
        ItemStack rightpotion = new ItemStack(bukkitstand, BrewingStandSlot.POTION_RIGHT.index());
        Set<ItemStack> potions = new HashSet<>();
        if(leftpotion.bukkitstack() != null) { potions.add(leftpotion); }
        if(middlepotion.bukkitstack() != null) { potions.add(middlepotion); }
        if(rightpotion.bukkitstack() != null) { potions.add(rightpotion); }

        // make sure there is an ingredient, fuel, and at least one potion to brew
        if(bukkitstand.getFuelLevel() == 0 ||
           ingredient.bukkitstack() == null ||
           potions.isEmpty()) {
            return;
        }

        // get the recipe that should be run
        BrewingRecipe recipe = BrewingRecipes.instance().get(ingredient, potions);
        if(recipe == null) {
            return;
        }

        // get all available fuel in the brewing stand
        int totalfuel = bukkitstand.getFuelLevel();
        ItemStack fuelstack = new ItemStack(bukkitstand.getSnapshotInventory().getFuel());
        if(fuelstack.bukkitstack() != null) {
            totalfuel += fuelstack.bukkitstack().getAmount() * BrewingRecipes.instance().fuel(fuelstack.bukkitstack().getType());
        }

        // check to see if there is enough fuel for the recipe
        if(totalfuel < recipe.fuelcost()) {
            return;
        }

        // consume recipe fuel
        int fuelneeded = recipe.fuelcost();
        while(fuelneeded > 0) {
            if(fuelneeded >= bukkitstand.getFuelLevel()) {
                // subtract current fuel from needed fuel
                fuelneeded -= bukkitstand.getFuelLevel();

                // remove all remaining fuel from brewing stand
                bukkitstand.setFuelLevel(0);

                // run the fuel update check
                checkfuel(bukkitstand);
            } else {
                // remove neededfuel from stand fuel
                bukkitstand.setFuelLevel(bukkitstand.getFuelLevel() - fuelneeded);

                // set fuel needed to 0
                fuelneeded = 0;
            }
        }

        // consume one ingredient
        bukkitstand.getSnapshotInventory().setItem(BrewingStandSlot.INGREDIENT.index(), ingredient.bukkitstack().asQuantity(ingredient.bukkitstack().getAmount() - 1));

        // create and start the recipe instance
        bukkitstand.setRecipeBrewTime(NORMAL_BREWING_TIME);
        bukkitstand.setBrewingTime(NORMAL_BREWING_TIME);
        bukkitstand.update();
        recipeinstance = new BrewingRecipeInstance(player, this, player.brewing().roll(), recipe);

        // save the brewing stand
        BrewingStands.instance().save(this);
    }

    public void updaterecipetimer(int ticks) {
        // get bukkit stand to access inventory and block data
        org.bukkit.block.BrewingStand bukkitstand = (org.bukkit.block.BrewingStand)location.block().getState();

        // update recipe ticks
        bukkitstand.setBrewingTime(NORMAL_BREWING_TIME * ticks / recipeinstance.recipe().brewtime());
        bukkitstand.update();

        // save every second of the tick timer
        if(ticks % 20 == 0) {
            BrewingStands.instance().save(this);
        }
    }

    public void finishedbrewing() {
        // get bukkit stand to access inventory and block data
        org.bukkit.block.BrewingStand bukkitstand = (org.bukkit.block.BrewingStand)location.block().getState();

        // apply the recipe to each potion
        for(BrewingStandSlot potionslot : BrewingStandSlot.potions()) {
            // get potion
            ItemStack potion = new ItemStack(bukkitstand, potionslot.index());

            // make sure potion exists and has a craftable to ensure there is a crafting roll
            if(potion.bukkitstack() != null && potion.craftable() != null) {
                int craftroll = potion.get(Key.CRAFT_VALUE_KEY, PersistentDataType.INTEGER);
                potion = recipeinstance.recipe().apply(recipeinstance.player(), potion, craftroll + recipeinstance.brewroll());
                bukkitstand.getSnapshotInventory().setItem(potionslot.index(), potion.bukkitstack());
            }
        }

        // update the block
        bukkitstand.update();

        // reset the instance state and save the info
        Player player = recipeinstance.player();
        recipeinstance = null;
        BrewingStands.instance().save(this);

        // check fuel and see if the next recipe can continue
        checkfuel();
        checkrecipe(player);
    }
}
