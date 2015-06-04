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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple class that provides methods to register, unregister, and run
 * execution for all registered plugins.
 * 
 * @author Blake Beaupain
 */
public class PluginHandler {

	/** All registered plugins. */
	private static List<Plugin> plugins = new ArrayList<Plugin>();

	/**
	 * Processes execution for all registered plugins.
	 */
	public static void process() {
		synchronized (plugins) {
			Iterator<Plugin> iter = plugins.iterator();
			while (iter.hasNext()) {
				try {
					iter.next().cycle();
				} catch (Exception ex) {
					ex.printStackTrace();
					iter.remove();
				}
			}
		}
	}

	/**
	 * Loads all plugins.
	 * 
	 * @throws Exception
	 */
	public static void loadPlugins() throws Exception {
		File file = new File("./plugins.ini");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String pluginName;
		while ((pluginName = reader.readLine()) != null) {
			@SuppressWarnings("unchecked")
			Class<Plugin> clazz = (Class<Plugin>) Class.forName(pluginName);
			register(clazz.newInstance());
		}
	}

	/**
	 * Registers a plugin and calls the plugin's onEnable method.
	 * 
	 * @param plugin
	 *            The plugin to register
	 */
	public static void register(Plugin plugin) {
		try {
			plugin.onEnable();
			synchronized (plugins) {
				plugins.add(plugin);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Unregisters a plugin and calls the plugin's onDisable method.
	 * 
	 * @param plugin
	 *            The plugin to unregister
	 */
	public static void unregister(Plugin plugin) {
		try {
			plugin.onDisable();
			synchronized (plugins) {
				plugins.remove(plugin);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
