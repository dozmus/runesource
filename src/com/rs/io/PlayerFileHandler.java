package com.rs.io;

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
