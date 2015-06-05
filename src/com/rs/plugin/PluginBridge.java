package com.rs.plugin;

import com.rs.entity.player.Player;
import com.rs.util.Misc;

/**
 * A bridge between invokable plugins and the core of the server.
 *
 * @author Pure_
 */
public final class PluginBridge {

    public static void handleCommand(Player player, String keyword, String[] args) {
        Misc.Stopwatch timer = new Misc.Stopwatch();
        PluginHandler.invokeMethod("bindings.CommandHandler", "handle", player, keyword, args);
        System.out.println(timer.elapsed());
    }
}
