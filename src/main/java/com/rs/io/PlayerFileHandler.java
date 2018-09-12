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

import com.rs.entity.player.PlayerAttributes;

/**
 * A player loader, and saver.
 *
 * @author Pure_
 */
public interface PlayerFileHandler {

    /**
     * Saves the argument attributes.
     */
    void save(PlayerAttributes attributes) throws Exception;

    /**
     * Returns the player attributes associated with the argument username.
     */
    PlayerAttributes load(String username) throws Exception;

    /**
     * Returns the directory this will store player flat files in.
     */
    String getStorageDirectory();
}
