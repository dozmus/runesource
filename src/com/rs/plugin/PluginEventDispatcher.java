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
import com.rs.plugin.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An event dispatcher which acts between invokable plugins and the core of the server.
 *
 * @author Pure_
 */
public final class PluginEventDispatcher {

    private static final HashMap<String, List<String>> bindings = new HashMap<>();
    public static final String ACTION_BUTTON_HANDLER_EVENT = "onActionButton";
    public static final String COMMAND_HANDLER_EVENT = "onCommand";
    /**
     * After the player is logged in.
     */
    public static final String PLAYER_ON_LOGIN_EVENT = "onLogin";
    /**
     * After the player is logged out.
     */
    public static final String PLAYER_ON_LOGOUT_EVENT = "onLogout";
    public static final String ADD_FRIEND_EVENT = "onAddFriend";
    public static final String REMOVE_FRIEND_EVENT = "onRemoveFriend";
    public static final String ADD_IGNORE_EVENT = "onAddIgnore";
    public static final String REMOVE_IGNORE_EVENT = "onRemoveIgnore";
    public static final String PRIVATE_MESSAGE_EVENT = "onPrivateMessage";
    /**
     * Before the new values are set in the {@link com.rs.entity.player.PlayerSettings}.
     */
    public static final String MODIFY_CHAT_MODE_EVENT = "onModifyChatMode";

    /**
     * Registers a binding.
     *
     * @param event event name
     * @param pluginName plugin name
     */
    public static void register(String event, String pluginName) {
        if (bindings.containsKey(event)) {
            bindings.get(event).add(pluginName);
        } else {
            List<String> pluginNames = new ArrayList<>();
            pluginNames.add(pluginName);
            bindings.put(event, pluginNames);
        }
    }

    public static boolean dispatchCommand(Player player, String commandName, String[] args) {
        if (!bindings.containsKey(COMMAND_HANDLER_EVENT)) {
            return false;
        }
        CommandEvent evt = new CommandEvent(player, commandName, args);

        for (String pluginName : bindings.get(COMMAND_HANDLER_EVENT)) {
            PluginHandler.invokeMethod(pluginName, COMMAND_HANDLER_EVENT, evt);
        }
        return true;
    }

    public static boolean dispatchActionButton(Player player, int actionButtonId) {
        if (!bindings.containsKey(ACTION_BUTTON_HANDLER_EVENT)) {
            return false;
        }
        ActionButtonEvent evt = new ActionButtonEvent(player, actionButtonId);

        for (String pluginName : bindings.get(ACTION_BUTTON_HANDLER_EVENT)) {
            PluginHandler.invokeMethod(pluginName, ACTION_BUTTON_HANDLER_EVENT, evt);
        }
        return true;
    }

    public static boolean dispatchLogin(Player player, boolean newPlayer) {
        if (!bindings.containsKey(PLAYER_ON_LOGIN_EVENT)) {
            return false;
        }
        PlayerLoggedOnEvent evt = new PlayerLoggedOnEvent(player, newPlayer);

        for (String pluginName : bindings.get(PLAYER_ON_LOGIN_EVENT)) {
            PluginHandler.invokeMethod(pluginName, PLAYER_ON_LOGIN_EVENT, evt);
        }
        return true;
    }

    public static boolean dispatchLogout(Player player) {
        if (!bindings.containsKey(PLAYER_ON_LOGOUT_EVENT)) {
            return false;
        }
        PlayerLoggedOutEvent evt = new PlayerLoggedOutEvent(player);

        for (String pluginName : bindings.get(PLAYER_ON_LOGOUT_EVENT)) {
            PluginHandler.invokeMethod(pluginName, PLAYER_ON_LOGOUT_EVENT, evt);
        }
        return true;
    }

    public static boolean dispatchAddIgnore(Player player, long name) {
        if (!bindings.containsKey(ADD_IGNORE_EVENT)) {
            return false;
        }
        ModifyIgnoredListEvent evt = new ModifyIgnoredListEvent(player, name, ModifyIgnoredListEvent.Type.ADD);

        for (String pluginName : bindings.get(ADD_IGNORE_EVENT)) {
            PluginHandler.invokeMethod(pluginName, ADD_IGNORE_EVENT, evt);
        }
        return true;
    }

    public static boolean dispatchRemoveIgnore(Player player, long name) {
        if (!bindings.containsKey(REMOVE_IGNORE_EVENT)) {
            return false;
        }
        ModifyIgnoredListEvent evt = new ModifyIgnoredListEvent(player, name, ModifyIgnoredListEvent.Type.REMOVE);

        for (String pluginName : bindings.get(REMOVE_IGNORE_EVENT)) {
            PluginHandler.invokeMethod(pluginName, REMOVE_IGNORE_EVENT, evt);
        }
        return true;
    }

    public static boolean dispatchAddFriend(Player player, long name) {
        if (!bindings.containsKey(ADD_FRIEND_EVENT)) {
            return false;
        }
        ModifyFriendsListEvent evt = new ModifyFriendsListEvent(player, name, ModifyFriendsListEvent.Type.ADD);

        for (String pluginName : bindings.get(ADD_FRIEND_EVENT)) {
            PluginHandler.invokeMethod(pluginName, ADD_FRIEND_EVENT, evt);
        }
        return true;
    }

    public static boolean dispatchRemoveFriend(Player player, long name) {
        if (!bindings.containsKey(REMOVE_FRIEND_EVENT)) {
            return false;
        }
        ModifyFriendsListEvent evt = new ModifyFriendsListEvent(player, name, ModifyFriendsListEvent.Type.REMOVE);

        for (String pluginName : bindings.get(REMOVE_FRIEND_EVENT)) {
            PluginHandler.invokeMethod(pluginName, REMOVE_FRIEND_EVENT, evt);
        }
        return true;
    }

    public static boolean dispatchPrivateMessage(Player player, long name, byte[] text) {
        if (!bindings.containsKey(PRIVATE_MESSAGE_EVENT)) {
            return false;
        }
        PrivateMessageEvent evt = new PrivateMessageEvent(player, name, text);

        for (String pluginName : bindings.get(PRIVATE_MESSAGE_EVENT)) {
            PluginHandler.invokeMethod(pluginName, PRIVATE_MESSAGE_EVENT, evt);
        }
        return true;
    }

    public static boolean dispatchModifyChatMode(Player player, int publicChatMode, int privateChatMode, int tradeMode) {
        if (!bindings.containsKey(MODIFY_CHAT_MODE_EVENT)) {
            return false;
        }
        ModifyChatModeEvent evt = new ModifyChatModeEvent(player, publicChatMode, privateChatMode, tradeMode);

        for (String pluginName : bindings.get(MODIFY_CHAT_MODE_EVENT)) {
            PluginHandler.invokeMethod(pluginName, MODIFY_CHAT_MODE_EVENT, evt);
        }
        return true;
    }
}
