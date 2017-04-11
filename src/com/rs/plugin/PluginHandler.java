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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple class that provides methods to register, unregister, and run
 * execution for all registered plugins.
 *
 * @author Blake Beaupain
 */
public final class PluginHandler {

    /**
     * The base directory of all plugins.
     */
    private static final String PLUGIN_DIRECTORY = "./plugins/";
    private static final GroovyClassLoader classLoader = new GroovyClassLoader();

    /**
     * All registered plugins.
     */
    private static final HashMap<String, Plugin> plugins = new HashMap<>();

    /**
     * Processes execution for all registered plugins.
     */
    public static void tick() throws Exception {
        for (Plugin plugin : plugins.values()) {
            plugin.tick();
        }
    }

    /**
     * Invokes a method from the given plugin.
     *
     * @param pluginName plugin name
     * @param method     method name
     * @param args       arguments
     */
    public static void invokeMethod(String pluginName, String method, Object... args) {
        // Attempting to fetch the plugin
        Plugin plugin = plugins.get(pluginName);

        if (plugin == null) {
            return;
        }

        // Invoking the method
        plugin.getInstance().invokeMethod(method, args);
    }

    /**
     * Loads all plugins.
     *
     * @param pluginsFile a text file containing a list of plugins to load
     * @throws Exception
     */
    public static void loadPlugins(String pluginsFile) throws Exception {
        File file = new File(pluginsFile);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String pluginName;

        while ((pluginName = reader.readLine()) != null) {
            if (pluginName.trim().length() == 0)
                continue;

            // Loading the plugin instance
            Class cls = classLoader.parseClass(new File(PLUGIN_DIRECTORY + pluginName + ".groovy"));
            GroovyObject obj = (GroovyObject) cls.newInstance();
            Plugin plugin = (Plugin) obj;
            plugin.setInstance(obj);
            register(pluginName.replace('/', '.'), plugin);
        }
    }

    /**
     * Registers a plugin and calls the plugin's onEnable method.
     *
     * @param name   The plugin name
     * @param plugin The plugin to register
     */
    public static void register(String name, Plugin plugin) {
        try {
            plugin.onEnable(name);
            plugins.put(name, plugin);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Unregisters a plugin and calls the plugin's onDisable method.
     *
     * @param plugin The plugin to unregister
     */
    public static void unregister(Plugin plugin) {
        for (Map.Entry<String, Plugin> entry : plugins.entrySet()) {
            if (entry.getValue().equals(plugin)) {
                unregister(entry.getKey());
            }
        }
    }

    /**
     * Unregisters a plugin and calls the plugin's onDisable method.
     *
     * @param name The plugin name to unregister
     */
    public static void unregister(String name) {
        try {
            plugins.get(name).onDisable();
            plugins.remove(name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
