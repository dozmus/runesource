package com.rs.io;
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
import com.rs.entity.player.PlayerAttributes;

/**
 * An abstract player file saving/loading description.
 *
 * @author Pure_
 */
public interface PlayerFileHandler {

    /**
     * Saves a player's attributes.
     */
    void save(PlayerAttributes attributes) throws Exception;


    /**
     * Loads a player's attributes into the argument instance.
     *
     * @param player the player to load
     * @return login response
     */
    LoadResponse load(Player player) throws Exception;

    /**
     * Returns the player attributes associated with the argument username.
     */
    PlayerAttributes load(String username) throws Exception;

    /**
     * Returns the directory to store player flat files in.
     */
    String getStorageDirectory();

    /**
     * A collection of possible load responses.
     *
     * @author Pure_
     */
    enum LoadResponse {
        SUCCESS, NOT_FOUND, INVALID_CREDENTIALS, BANNED
    }
}
