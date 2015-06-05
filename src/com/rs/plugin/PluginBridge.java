package com.rs.plugin;

import com.rs.entity.player.Player;

import java.util.HashMap;

/**
 * A bridge between invokable plugins and the core of the server.
 *
 * @author Pure_
 */
public final class PluginBridge {

    private static HashMap<String, String> bindings = new HashMap<String, String>();
    public static final String ACTION_BUTTON_HANDLER_BINDING_KEY = "bindings.packets.ActionButtonHandler";
    public static final String COMMAND_HANDLER_BINDING_KEY = "bindings.packets.CommandHandler";

    /**
     * Registers a binding.
     *
     * @param binding binding internal name
     * @param pluginName plugin name
     */
    public static void registerBinding(String binding, String pluginName) {
        bindings.put(binding, pluginName);
    }

    public static boolean handleCommand(Player player, String keyword, String[] args) {
        if (!bindings.containsKey(COMMAND_HANDLER_BINDING_KEY)) {
            return false;
        }
        PluginHandler.invokeMethod(bindings.get(COMMAND_HANDLER_BINDING_KEY), "handle", player, keyword, args);
        return true;
    }

    public static boolean handleActionButton(Player player, int actionButtonId) {
        if (!bindings.containsKey(ACTION_BUTTON_HANDLER_BINDING_KEY)) {
            return false;
        }
        PluginHandler.invokeMethod(bindings.get(ACTION_BUTTON_HANDLER_BINDING_KEY), "handle", player, actionButtonId);
        return true;
    }
}
