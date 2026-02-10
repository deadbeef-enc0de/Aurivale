package net.mctitan.rpg.data.brewing;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.mctitan.data.BasicData;
import net.mctitan.data.UUID;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.brewing.BrewingStands;
import net.mctitan.rpg.brewing.recipe.BrewingRecipe;
import net.mctitan.rpg.brewing.BrewingRecipes;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.util.Logger;
import net.mctitan.rpg.util.directional.Location;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class BrewingRecipeInstance extends BasicData {
    private UUID player;
    private UUID brewingstand;
    private int brewroll;
    private BrewingRecipe recipe;
    private int runtime = 0;
    private transient ScheduledTask task;

    public BrewingRecipeInstance() {}

    public BrewingRecipeInstance(Player player, BrewingStand brewingstand, int brewroll, BrewingRecipe recipe) {
        this.player = player.uuid;
        this.brewingstand = brewingstand.uuid;
        this.brewroll = brewroll;
        this.recipe = recipe;
        this.runtime = recipe.brewtime();

        start(brewingstand().location());
    }

    public Player player() { return DataManager.instance().player(player); }
    public BrewingStand brewingstand() { return BrewingStands.instance().stand(brewingstand); }
    public int brewroll() { return brewroll; }
    public BrewingRecipe recipe() { return recipe; }
    public int runtime() { return runtime; }

    public void initialize() {
        // reload the brewing recipe if this has one
        if(recipe != null) {
            recipe = BrewingRecipes.instance().get(recipe.name());
        }

        // restart the brewing task
        start(brewingstand().location());
    }

    public void start(Location location) {
        // if we already have a task, do nothing
        if(task != null) {
            return;
        }

        Logger.LOG(Level.INFO, "Brewing Runner Started");

        // start the recipe instance
        task = Bukkit.getRegionScheduler().runAtFixedRate(Aurivale.instance(), location.bukkit(), task -> {
            // increase the tick count
            --runtime;

            // update the brewing stand recipe timer
            brewingstand().updaterecipetimer(runtime);

            // check to see if we are done
            if(runtime <= 0) {
                // remove the scheduled task
                stop();

                // tell the brewing stand it's finished
                brewingstand().finishedbrewing();

            }
        }, 1, 1);
    }

    public void stop() {
        Logger.LOG(Level.INFO, "Brewing Runner Stopped");
        task.cancel();
        task = null;
    }
}
