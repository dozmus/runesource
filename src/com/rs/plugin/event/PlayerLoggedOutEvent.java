package com.rs.plugin.event;

import com.rs.entity.player.Player;
import com.rs.plugin.PlayerEvent;

/**
 * Player logged out event.
 */
public final class PlayerLoggedOutEvent extends PlayerEvent {

    public PlayerLoggedOutEvent(Player player) {
        super(player);
    }
}
