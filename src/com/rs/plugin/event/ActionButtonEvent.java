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
