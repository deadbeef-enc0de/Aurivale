package net.mctitan.rpg.data.message;

import net.mctitan.data.BasicData;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.Player;

import java.util.ArrayList;
import java.util.Iterator;

public class ActionMessageBar extends BasicData {
    private transient Player player;
    private ArrayList<Message> messages = new ArrayList<>();
    private int index = 0;

    public ActionMessageBar() {}

    public void player(Player player) {
        this.player = player;

        player.bukkitplayer().getScheduler().runAtFixedRate(Aurivale.instance(), task -> { tick(); }, null, 1, 20);
    }

    public void add(Message message) {
        messages.add(message);
    }

    public void tick() {
        // if there are no messages, do nothing
        if(messages.isEmpty()) {
            return;
        }

        // display the current message
        player.bukkitplayer().sendActionBar(messages.get(index).message());

        // tick each message
        Iterator<Message> iterator = messages.iterator();
        int curindex = 0;
        while (iterator.hasNext()) {
            // get the message
            Message message = iterator.next();

            // check the message and see if it is removed
            if(!message.tick()) {
                // remove the message
                iterator.remove();
                message.removed(player);

                // if the index is after this one we decrement it since it has moved to a lower index
                if(index > curindex) {
                    --index;
                }
            } else {
                // not removed we increment the local index variable
                ++curindex;
            }
        }

        // make sure index still works
        ++index;
        if(index >= messages.size()) { index = 0; }
    }
}
