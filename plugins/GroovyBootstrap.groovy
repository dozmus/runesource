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


import com.rs.plugin.EventListener
import com.rs.plugin.PluginHandler
import com.rs.plugin.Bootstrap
import com.rs.plugin.event.*
import com.rs.plugin.listener.*
import groovy.io.FileType

/**
 * The plugin bootstrap, this is the object which receives events from the Java portion of the server.
 * It then forwards these events to the groovy plugins which it has loaded, depending on which interfaces
 * each extends.
 * Plugins extending Bootstrap are not loaded by this class, they are only loaded in the PluginHandler.
 * This must extend and implement the methods of each interface in com.rs.plugin.listener which it desires to handle.
 */
class GroovyBootstrap implements Bootstrap {

    private static final GroovyClassLoader CLASS_LOADER = new GroovyClassLoader()
    Map<Class, List<Object>> plugins = new HashMap<>()

    void resetPlugins() {
        getPlugins(PluginStateListener.class).forEach { l -> l.unloaded() }
        plugins.clear()
    }

    void addPlugin(Class interfaceClass, Object instance) {
        getPlugins(interfaceClass).add(instance)
    }

    List<Object> getPlugins(Class interfaceClass) {
        if (!plugins.containsKey(interfaceClass)) {
            plugins.put interfaceClass, new ArrayList<>()
        }
        return plugins.get(interfaceClass)
    }

    boolean hasInterface(Class clazz, Class target) {
        if (target in clazz.getInterfaces()) {
            return true
        } else {
            for (Class i in clazz.getInterfaces()) {
                if (hasInterface(i, target)) {
                    return true
                }
            }
            return false
        }
    }

    /**
     * Loads all supported plugins, as defined by SUPPORTED_INTERFACES.
     * This also performs the functionality of a reload.
     */
    void load() {
        // Clear plugins
        resetPlugins()

        // Load new plugins
        File pluginsDir = new File(PluginHandler.PLUGINS_DIRECTORY)
        pluginsDir.eachFileRecurse(FileType.FILES) { file ->
            if (!file.name.endsWith('.groovy'))
                return

            Class cls = CLASS_LOADER.parseClass(file)

            // Skip if its a bootstrap
            if (Bootstrap.class in cls.getInterfaces()) {
                return
            }

            // Load
            Object obj = cls.newInstance()
            cls.getInterfaces().each { i ->
                if (hasInterface(i, EventListener.class)) {
                    addPlugin(i, obj)
                }
            }
        }

        // Dispatch loaded events
        getPlugins(PluginStateListener.class).forEach { l -> l.loaded() }
    }

    void actionButton(ActionButtonEvent e) {
        getPlugins(ActionButtonListener.class).forEach { l -> l.actionButton e }
    }

    void command(CommandEvent e) {
        getPlugins(CommandListener.class).forEach { l -> l.command e }
    }

    void modifyChatMode(ModifyChatModeEvent e) {
        getPlugins(MessageConfigListener.class).forEach { l -> l.modifyChatMode e }
    }

    void addFriend(ModifyPlayerListEvent e) {
        getPlugins(MessageConfigListener.class).forEach { l -> l.addFriend e }
    }

    void removeFriend(ModifyPlayerListEvent e) {
        getPlugins(MessageConfigListener.class).forEach { l -> l.removeFriend e }
    }

    void addIgnore(ModifyPlayerListEvent e) {
        getPlugins(MessageConfigListener.class).forEach { l -> l.addIgnore e }
    }

    void removeIgnore(ModifyPlayerListEvent e) {
        getPlugins(MessageConfigListener.class).forEach { l -> l.removeIgnore e }
    }

    void publicMessage(PublicMessageEvent e) {
        getPlugins(MessageListener.class).forEach { l -> l.publicMessage e }
    }

    void privateMessage(PrivateMessageEvent e) {
        getPlugins(MessageListener.class).forEach { l -> l.privateMessage e }
    }

    void logIn(PlayerLoggedInEvent e) {
        getPlugins(PlayerConnectivityListener.class).forEach { l -> l.logIn e }
    }

    void logOut(PlayerLoggedOutEvent e) {
        getPlugins(PlayerConnectivityListener.class).forEach { l -> l.logOut e }
    }

    void loaded() {
        getPlugins(PluginStateListener.class).forEach { l -> l.loaded() }
    }

    void unloaded() {
        getPlugins(PluginStateListener.class).forEach { l -> l.unloaded() }
    }

    void tick() {
        getPlugins(TickListener.class).forEach { l -> l.tick() }
    }
}
