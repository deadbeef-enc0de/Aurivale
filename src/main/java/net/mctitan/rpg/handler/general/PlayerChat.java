package net.mctitan.rpg.handler.general;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.DataManager;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.util.directional.ChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class PlayerChat implements Listener {
    private static final PlayerChat instance = new PlayerChat();

    private final int chatdistance;
    private final int chunkticks;

    private final List<Chat> added =  new LinkedList<>();
    private final List<Chat> chats = new LinkedList<>();

    private PlayerChat() {
        // get chat distance
        chatdistance = Aurivale.instance().getConfig("players").getInt("max_chat_range");
        chunkticks = Aurivale.instance().getConfig("players").getInt("chat_chunk_tick");

        // setup running task for chats
        executor();
    }

    public static PlayerChat instance() { return instance; }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        // create chat message
        Player player = DataManager.instance().player(event.getPlayer());
        Component message = Component.text(String.format("<%s> ", player.name())).append(event.message());
        Chat chat = new Chat(player, message);

        // add chat message to system for running
        synchronized (added) { added.add(chat); }

        // log to raw bukkit logger to look like it's a normal chat message
        String rawmessage = PlainTextComponentSerializer.plainText().serialize(message);
        Bukkit.getLogger().info(rawmessage); // yeah, yeah, yell at me for internal usage all you want

        // cancel original event
        event.setCancelled(true);
    }

    private void executor() {
        // run the executor in the global region thread
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(Aurivale.instance(), task -> {
            // get all new chat messages and add them to the running list
            synchronized (added) {
                if(!added.isEmpty()) {
                    chats.addAll(added);
                    added.clear();
                }
            }

            // if there are no chats, bail
            if(chats.isEmpty()) {
                return;
            }

            // get all players and setup mapping data
            Map<World, Map<ChunkLocation, List<Player>>> players = new HashMap<>();
            for(org.bukkit.entity.Player bukkitplayer : Bukkit.getOnlinePlayers()) {
                // get player and location
                Player player = DataManager.instance().player(bukkitplayer);
                ChunkLocation chunkloc =  new ChunkLocation(bukkitplayer.getLocation());
                World world = chunkloc.world();

                // add mapping for world and chunk location if necessary
                if(!players.containsKey(world)) { players.put(world, new HashMap<>()); }
                if(!players.get(world).containsKey(chunkloc)) { players.get(world).put(chunkloc, new LinkedList<>()); }

                // add player
                players.get(world).get(chunkloc).add(player);
            }

            // iterate through the chats
            Iterator<Chat> iter =  chats.iterator();
            while(iter.hasNext()) {
                Chat chat = iter.next();
                chat.step(players.getOrDefault(chat.world(), Map.of()));
                if(!chat.valid()) { iter.remove(); }
            }
        }, 1, chunkticks);
    }

    private class Chat {
        private final ChunkLocation location;
        private final Component message;
        private final Set<UUID> received = new HashSet<>();

        private int distance = 0;

        public Chat(Player player, Component message) {
            this.location = new ChunkLocation(player.bukkitplayer().getLocation());
            this.message = message;
        }

        public boolean valid() { return distance < chatdistance; }
        public World world() { return location.world(); }

        public void step(Map<ChunkLocation, List<Player>> players) {
            // make sure there are players to receieve the message
            if(!players.isEmpty()) {
                // get send list for message
                LinkedList<Player> sendlist = new LinkedList<>();
                if (distance > 0) {
                    // basically we send messages out in an expanding ring from the sending chunk
                    for (int d = 0; d < 2 * distance; ++d) {
                        sendlist.addAll(players.getOrDefault(location.add(distance, distance - d), List.of()));
                        sendlist.addAll(players.getOrDefault(location.add(distance - d, -distance), List.of()));
                        sendlist.addAll(players.getOrDefault(location.add(-distance, -distance + d), List.of()));
                        sendlist.addAll(players.getOrDefault(location.add(-distance + d, distance), List.of()));
                    }
                } else {
                    sendlist.addAll(players.getOrDefault(location, List.of()));
                }

                // send the message
                for (Player player : sendlist) {
                    // don't send message to a player twice
                    if(received.contains(player.uuid.uuid())) { continue; }

                    // send message and log it
                    player.bukkitplayer().sendMessage(message);
                    received.add(player.uuid.uuid());
                }
            }

            // increment distance
            ++distance;
        }
    }
}
