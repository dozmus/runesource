package com.rs.plugin;

import com.rs.entity.player.Player;

/**
 * An {@link Event} triggered by a {@link Player}.
 */
public abstract class PlayerEvent implements Event {

    protected final Player player;

    public PlayerEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
