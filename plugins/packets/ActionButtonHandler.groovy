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
import com.rs.entity.player.Player
import com.rs.plugin.Plugin
import com.rs.plugin.PluginEventDispatcher
import com.rs.plugin.event.ActionButtonEvent

class ActionButtonHandler extends Plugin {

    void onActionButton(ActionButtonEvent evt) {
        Player player = evt.getPlayer()

        switch (evt.getActionButtonId()) {
            case 9154:
                player.sendLogout()
                break
            case 153:
                player.getAttributes().getSettings().setRunToggled true
                break
            case 152:
                player.getAttributes().getSettings().setRunToggled false
                break
            default:
                println "Unhandled button: ${evt.getActionButtonId()}"
                break
        }
    }

    @Override
    void tick() throws Exception {
    }

    @Override
    void onEnable(String pluginName) throws Exception {
        PluginEventDispatcher.register PluginEventDispatcher.ACTION_BUTTON_HANDLER_EVENT, pluginName
    }

    @Override
    void onDisable() throws Exception {
    }
}
