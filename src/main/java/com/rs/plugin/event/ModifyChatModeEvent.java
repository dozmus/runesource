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
 * An event representing a change in chat mode.
 */
public final class ModifyChatModeEvent extends PlayerEvent {

    private int publicChatMode;
    private int privateChatMode;
    private int tradeMode;

    public ModifyChatModeEvent(Player player, int publicChatMode, int privateChatMode, int tradeMode) {
        super(player);
        this.publicChatMode = publicChatMode;
        this.privateChatMode = privateChatMode;
        this.tradeMode = tradeMode;
    }

    public int getPublicChatMode() {
        return publicChatMode;
    }

    public int getPrivateChatMode() {
        return privateChatMode;
    }

    public int getTradeMode() {
        return tradeMode;
    }
}
