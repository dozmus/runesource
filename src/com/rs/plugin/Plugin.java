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

import groovy.lang.GroovyObject;

/**
 * An interface that allows users to create objects that run along side the main
 * execution of the server.
 *
 * @author Pure_
 * @author blakeman8192
 */
public abstract class Plugin {

    private GroovyObject instance;

    /**
     * Called every time the server performs a cycle.
     *
     * @throws Exception If the plugin throws any form of exception
     */
    public abstract void cycle() throws Exception;

    /**
     * Called when the plugin is enabled.
     *
     * @param pluginName The name of this plugin from the plugins list file
     * @throws Exception If the plugin throws any form of exception
     */
    public abstract void onEnable(String pluginName) throws Exception;

    /**
     * Called when the plugin is disabled.
     *
     * @throws Exception If the plugin is disabled
     */
    public abstract void onDisable() throws Exception;

    /**
     * Get the groovy object instance of the plugin.
     *
     * @return
     */
    public GroovyObject getInstance() {
        return instance;
    }

    /**
     * Set the groovy object instance of this plugin
     *
     * @param instance
     */
    public void setInstance(GroovyObject instance) {
        if (this.instance != null) {
            throw new IllegalStateException("Plugin already loaded.");
        }
        this.instance = instance;
    }
}
