package com.rs.plugin.event;

import com.rs.entity.player.Player;
import com.rs.plugin.PlayerEvent;

/**
 * Private message event.
 */
public class PrivateMessageEvent extends PlayerEvent {

    private final long target;
    private final byte[] text;

    public PrivateMessageEvent(Player player, long target, byte[] text) {
        super(player);
        this.target = target;
        this.text = text;
    }

    /**
     * The player's username id who is being messaged.
     */
    public long getTarget() {
        return target;
    }

    public byte[] getText() {
        return text;
    }
}
