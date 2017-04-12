package com.rs.plugin.event;

import com.rs.entity.player.Player;
import com.rs.plugin.PlayerEvent;

/**
 * A player command event.
 */
public final class CommandEvent extends PlayerEvent {

    private final String commandName;
    private final String[] args;

    public CommandEvent(Player player, String commandName, String[] args) {
        super(player);
        this.commandName = commandName;
        this.args = args;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}
