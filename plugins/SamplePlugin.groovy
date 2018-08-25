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
import com.rs.plugin.listener.PluginStateListener
import com.rs.plugin.listener.TickListener

/**
 * A sample plugin, which extends TickListener which tells it when a server tick occurs, and PluginStateListener
 * which tells it when it is loaded or unloaded.
 * It can know when more events occur by implementing interfaces from the com.rs.plugin.listener package.
 */
class SamplePlugin implements TickListener, PluginStateListener {

    @Override
    void tick() throws Exception {
        // Code to execute on tick
    }

    @Override
    void loaded() throws Exception {
        // Code to execute when plugin is enabled
    }

    @Override
    void unloaded() throws Exception {
        // Code to execute when plugin is disabled
    }
}
