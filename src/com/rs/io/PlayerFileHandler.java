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

/**
 * @author Pure_
 */
public interface PlayerFileHandler {

    /**
     * Saves a player's attributes.
     *
     * @param player the player to save
     */
    public void save(Player player) throws Exception;


    /**
     * Loads a player's attributes.
     *
     * @param player the player to load.
     * @return login response
     */
    public LoadResponse load(Player player) throws Exception;

    /**
     * The directory to store player files.
     *
     * @return player file directory
     */
    public String getDirectory();

    /**
     * A collection of possible load responses.
     *
     * @author Pure_
     */
    public enum LoadResponse {
        SUCCESS, NOT_FOUND, INVALID_CREDENTIALS
    }
}
