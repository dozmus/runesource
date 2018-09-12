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

/**
 * Private message event.
 */
public final class PrivateMessageEvent extends PlayerEvent {

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
