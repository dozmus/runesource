package com.rs.plugin.event;

import com.rs.entity.player.Player;
import com.rs.plugin.PlayerEvent;

/**
 * Player logged on event.
 */
public final class PlayerLoggedOnEvent extends PlayerEvent {

    public PlayerLoggedOnEvent(Player player) {
        super(player);
    }
}
