package com.rs.plugin.event;

import com.rs.entity.player.Player;
import com.rs.plugin.PlayerEvent;

/**
 * An event representing either the addition, or the removal of a friend.
 */
public final class ModifyFriendsListEvent extends PlayerEvent {

    private final long target;
    private final Type type;

    public ModifyFriendsListEvent(Player player, long target, Type type) {
        super(player);
        this.target = target;
        this.type = type;
    }

    /**
     * The player's username id who was added or removed from the friends list.
     */
    public long getTarget() {
        return target;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        ADD, REMOVE
    }
}
