package com.rs.plugin;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A bridge between invokable plugins and the core of the server.
 *
 * @author Pure_
 */
public final class PluginBridge {

    private static HashMap<String, List<String>> bindings = new HashMap<>();
    public static final String ACTION_BUTTON_HANDLER_EVENT = "onActionButton";
    public static final String COMMAND_HANDLER_EVENT = "onCommand";
    /**
     * After the player is logged in.
     */
    public static final String PLAYER_ON_LOGIN_EVENT = "onLogin";
    /**
     * Before the player is logged out.
     */
    public static final String PLAYER_ON_LOGOUT_EVENT = "onLogout";
    public static final String ADD_FRIEND_EVENT = "onAddFriend";
    public static final String REMOVE_FRIEND_EVENT = "onRemoveFriend";
    public static final String PRIVATE_MESSAGE_EVENT = "onPrivateMessage";

    /**
     * Registers a binding.
     *
     * @param event event name
     * @param pluginName plugin name
     */
    public static void registerEvent(String event, String pluginName) {
        if (bindings.containsKey(event)) {
            bindings.get(event).add(pluginName);
        } else {
            List<String> pluginNames = new ArrayList<>();
            pluginNames.add(pluginName);
            bindings.put(event, pluginNames);
        }
    }

    public static boolean triggerCommand(Player player, String keyword, String[] args) {
        if (!bindings.containsKey(COMMAND_HANDLER_EVENT)) {
            return false;
        }

        for (String pluginName : bindings.get(COMMAND_HANDLER_EVENT)) {
            PluginHandler.invokeMethod(pluginName, COMMAND_HANDLER_EVENT, player, keyword, args);
        }
        return true;
    }

    public static boolean triggerActionButton(Player player, int actionButtonId) {
        if (!bindings.containsKey(ACTION_BUTTON_HANDLER_EVENT)) {
            return false;
        }

        for (String pluginName : bindings.get(ACTION_BUTTON_HANDLER_EVENT)) {
            PluginHandler.invokeMethod(pluginName, ACTION_BUTTON_HANDLER_EVENT, player, actionButtonId);
        }
        return true;
    }

    public static boolean triggerOnLogin(Player player) {
        if (!bindings.containsKey(PLAYER_ON_LOGIN_EVENT)) {
            return false;
        }

        for (String pluginName : bindings.get(PLAYER_ON_LOGIN_EVENT)) {
            PluginHandler.invokeMethod(pluginName, PLAYER_ON_LOGIN_EVENT, player);
        }
        return true;
    }

    public static boolean triggerOnLogout(Player player) {
        if (!bindings.containsKey(PLAYER_ON_LOGOUT_EVENT)) {
            return false;
        }

        for (String pluginName : bindings.get(PLAYER_ON_LOGOUT_EVENT)) {
            PluginHandler.invokeMethod(pluginName, PLAYER_ON_LOGOUT_EVENT, player);
        }
        return true;
    }

    public static boolean triggerAddFriend(Player player, long name) {
        if (!bindings.containsKey(ADD_FRIEND_EVENT)) {
            return false;
        }

        for (String pluginName : bindings.get(ADD_FRIEND_EVENT)) {
            PluginHandler.invokeMethod(pluginName, ADD_FRIEND_EVENT, player, name);
        }
        return true;
    }

    public static boolean triggerRemoveFriend(Player player, long name) {
        if (!bindings.containsKey(REMOVE_FRIEND_EVENT)) {
            return false;
        }

        for (String pluginName : bindings.get(REMOVE_FRIEND_EVENT)) {
            PluginHandler.invokeMethod(pluginName, REMOVE_FRIEND_EVENT, player, name);
        }
        return true;
    }

    public static boolean triggerPrivateMessage(Player player, long name, byte[] text) {
        if (!bindings.containsKey(PRIVATE_MESSAGE_EVENT)) {
            return false;
        }

        for (String pluginName : bindings.get(PRIVATE_MESSAGE_EVENT)) {
            PluginHandler.invokeMethod(pluginName, PRIVATE_MESSAGE_EVENT, player, name, text);
        }
        return true;
    }
}
