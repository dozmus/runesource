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

import com.rs.entity.action.PublicChat;
import com.rs.entity.player.Player;
import com.rs.entity.player.infractions.ReportAbuse;
import com.rs.plugin.event.*;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple class that provides methods to register, unregister, and run
 * execution for all registered plugins.
 *
 * @author Pure_
 */
public final class PluginHandler {

    // TODO dispatch loaded() + unloaded()

    /**
     * The base directory of all plugins.
     */
    public static final String PLUGINS_DIRECTORY = "./plugins/";
    private static final GroovyClassLoader classLoader = new GroovyClassLoader();
    /**
     * All registered bootstraps.
     */
    private static final List<Bootstrap> bootstraps = new ArrayList<>();

    /**
     * Loads all bootstraps.
     */
    public static void load() throws Exception {
        boolean foundBootstrap = false;

        try (Stream<Path> paths = Files.walk(Paths.get(PLUGINS_DIRECTORY))) {
            for (File f : paths.filter(p -> !Files.isDirectory(p) && p.getFileName().toString().endsWith(".groovy"))
                    .map(Path::toFile)
                    .collect(Collectors.toList())) {
                Class cls = classLoader.parseClass(f);

                if (Arrays.asList(cls.getInterfaces()).contains(Bootstrap.class)) {
                    Bootstrap b = (Bootstrap) cls.getDeclaredConstructor().newInstance();
                    b.load();
                    bootstraps.add(b);
                    foundBootstrap = true;
                }
            }
        }

        if (!foundBootstrap) {
            System.err.println("Unable to find a suitable bootstrap!");
        }
    }

    /**
     * Reloads all plugins, by calling {@link Bootstrap#load()}.
     */
    public static void reload() {
        bootstraps.forEach(Bootstrap::load);
    }

    /**
     * Performs an action on each registered bootstrap.
     * @param action
     */
    public static void forEach(Consumer<? super Bootstrap> action) {
        bootstraps.forEach(action);
    }

    public static void dispatchLogin(Player player, boolean newPlayer) {
        PlayerLoggedInEvent e = new PlayerLoggedInEvent(player, newPlayer);
        forEach(b -> b.logIn(e));
    }

    public static void dispatchLogout(Player player) {
        PlayerLoggedOutEvent e = new PlayerLoggedOutEvent(player);
        forEach(b -> b.logOut(e));
    }

    public static void dispatchModifyChatMode(Player player, int publicChatMode, int privateChatMode, int tradeMode) {
        ModifyChatModeEvent e = new ModifyChatModeEvent(player, publicChatMode, privateChatMode, tradeMode);
        forEach(b -> b.modifyChatMode(e));
    }

    public static void dispatchActionButton(Player player, int actionButtonId) {
        ActionButtonEvent e = new ActionButtonEvent(player, actionButtonId);
        forEach(b -> b.actionButton(e));
    }

    public static void dispatchAddIgnore(Player player, long target) {
        ModifyPlayerListEvent e = new ModifyPlayerListEvent(player, target);
        forEach(b -> b.addIgnore(e));
    }

    public static void dispatchRemoveIgnore(Player player, long target) {
        ModifyPlayerListEvent e = new ModifyPlayerListEvent(player, target);
        forEach(b -> b.removeIgnore(e));
    }

    public static void dispatchAddFriend(Player player, long target) {
        ModifyPlayerListEvent e = new ModifyPlayerListEvent(player, target);
        forEach(b -> b.addFriend(e));
    }

    public static void dispatchRemoveFriend(Player player, long target) {
        ModifyPlayerListEvent e = new ModifyPlayerListEvent(player, target);
        forEach(b -> b.removeFriend(e));
    }

    public static void dispatchPrivateMessage(Player player, long target, byte[] text) {
        PrivateMessageEvent e = new PrivateMessageEvent(player, target, text);
        forEach(b -> b.privateMessage(e));
    }

    public static void dispatchCommand(Player player, String commandName, String[] args) {
        CommandEvent e = new CommandEvent(player, commandName, args);
        forEach(b -> b.command(e));
    }

    public static void dispatchPublicMessage(Player player, PublicChat publicChat) {
        PublicMessageEvent e = new PublicMessageEvent(player, publicChat);
        forEach(b -> b.publicMessage(e));
    }

    public static void dispatchReportAbuse(Player player, ReportAbuse reportAbuse) {
        ReportAbuseEvent e = new ReportAbuseEvent(player, reportAbuse);
        forEach(b -> b.reportAbuse(e));
    }

    public static void dispatchTick() throws Exception {
        for (Bootstrap b : bootstraps) {
            b.tick();
        }
    }
}
