package com.rs.plugin.event;

import com.rs.entity.player.Player;
import com.rs.plugin.PlayerEvent;

/**
 * An action button click event.
 */
public final class ActionButtonEvent extends PlayerEvent {

    private final int actionButtonId;

    public ActionButtonEvent(Player player, int actionButtonId) {
        super(player);
        this.actionButtonId = actionButtonId;
    }

    public int getActionButtonId() {
        return actionButtonId;
    }
}
