package com.rs.plugin.event;
/*
 * This file is part of RuneSource.
 *
 * RuneSource is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RuneSource is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RuneSource.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.rs.entity.player.Player;
import com.rs.plugin.PlayerEvent;

/**
 * An event representing either the addition, or the removal of a friend.
 */
public final class ModifyIgnoredListEvent extends PlayerEvent {

    private final long target;
    private final Type type;

    public ModifyIgnoredListEvent(Player player, long target, Type type) {
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
