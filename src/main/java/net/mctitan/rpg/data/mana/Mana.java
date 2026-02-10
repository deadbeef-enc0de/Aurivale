package net.mctitan.rpg.data.mana;

import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.event.PlayerStatsUpdateEvent;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.ManaModifier;

import java.util.HashMap;
import java.util.Map;

public class Mana extends Modable {
    private transient Player player;

    // variables used to calculate max mana and mana regen
    private transient double manabase = 0;
    private transient double manascaler = 1;
    private transient Map<Modifier, Double> manamultis = new HashMap<>();
    private transient double regenbase = 0;
    private transient double regenscaler = 1;
    private transient Map<Modifier, Double> regenmultis = new HashMap<>();

    // mana values
    private transient int tick = 0;
    private transient int maxmana;
    private transient double manaregen;
    private double mana = 0;

    public double value() { return mana; }
    public int maximum() { return maxmana; }
    public double regen()  { return manaregen; }

    public void player(Player player) {
        // set player object
        this.player = player;

        // make sure mana is below max mana
        if(mana > maxmana) { mana = maxmana; }

        // setup the task runner
        player.bukkitplayer().getScheduler().runAtFixedRate(Aurivale.instance(), task -> {
            // apply mana regen per tick
            synchronized (this) {
                // add mana regen
                change(manaregen);

                // update player
                this.player.showmana(mana, maxmana);

                // increment tick and every 5 ticks send PlayerStatsUpdateEvent
                tick = (tick + 1) % 5;
                if(tick == 0) {
                    PlayerStatsUpdateEvent event = new PlayerStatsUpdateEvent(this.player);
                    event.callEvent();
                }
            }
        }, null, 1, 1);
    }

    public void change(double amount) {
        synchronized (this) {
            mana += amount;
            if(mana < 0) { mana = 0; }
            if(mana > maxmana) { mana = maxmana; }
        }

        if(amount < 0) {
            PlayerStatsUpdateEvent event = new PlayerStatsUpdateEvent(this.player);
            event.callEvent();
        }
    }

    private void calculatemana() {
        double maxmana = manabase * manascaler;
        for(Double multi : manamultis.values()) {
            maxmana *= (1 + multi);
        }
        this.maxmana = (int) maxmana;
    }

    private void calculateregen() {
        // calculate mana regen with a base regen of 2.5%/sec
        manaregen = (regenbase + 0.025 * maxmana) * regenscaler;
        for(Double multi : regenmultis.values()) {
            manaregen *= ( 1 + multi);
        }

        // convert from per second to per tick
        manaregen /= 20;
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof ManaModifier manamod) {
            switch (manamod.manatype()) {
                case MAX_MANA -> {
                    switch (manamod.operator()) {
                        case FLAT -> manabase += manamod.value();
                        case SCALER -> manascaler += manamod.value() / 100d;
                        case MULTIPLIER -> manamultis.put(modifier, manamod.value() / 100d);
                    }
                    calculatemana();
                    calculateregen();
                }
                case MANA_REGEN -> {
                    switch (manamod.operator()) {
                        case FLAT -> regenbase += manamod.value();
                        case SCALER -> regenscaler += manamod.value() / 100d;
                        case MULTIPLIER -> regenmultis.put(modifier, manamod.value() / 100d);
                    }
                    calculateregen();
                }
            }
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof ManaModifier manamod) {
            switch (manamod.manatype()) {
                case MAX_MANA -> {
                    switch (manamod.operator()) {
                        case FLAT -> manabase -= manamod.value();
                        case SCALER -> manascaler -= manamod.value() / 100d;
                        case MULTIPLIER -> manamultis.remove(modifier);
                    }
                    calculatemana();
                    calculateregen();
                }
                case MANA_REGEN -> {
                    switch (manamod.operator()) {
                        case FLAT -> regenbase -= manamod.value();
                        case SCALER -> regenscaler -= manamod.value() / 100d;
                        case MULTIPLIER -> regenmultis.remove(modifier);
                    }
                    calculateregen();
                }
            }
        }
    }
}
