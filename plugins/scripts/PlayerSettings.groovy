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
import com.rs.plugin.event.PlayerLoggedOnEvent

class PlayerSettings extends Plugin {

    void onLogin(PlayerLoggedOnEvent evt) throws Exception {
        Player player = evt.getPlayer()
        com.rs.entity.player.PlayerSettings settings = player.getAttributes().getSettings()

        // Send initial setting states
        player.sendClientSetting(166, settings.getBrightness().settingValue())
        player.sendClientSetting(170, settings.getMouseButtons() == com.rs.entity.player.PlayerSettings.MouseButtons.ONE ? 1 : 0)
        player.sendClientSetting(171, settings.isChatEffects() ? 0 : 1)
        player.sendClientSetting(172, settings.isAutoRetaliate() ? 0 : 1)
        player.sendClientSetting(173, settings.isRunToggled() ? 1 : 0)
        player.sendClientSetting(287, settings.isSplitPrivateChat() ? 1 : 0)
        player.sendClientSetting(427, settings.isAcceptAid() ? 1 : 0)

        // Send chat mode
        player.sendChatModes()
    }

    void onActionButton(ActionButtonEvent evt) {
        Player player = evt.getPlayer()
        com.rs.entity.player.PlayerSettings settings = player.getAttributes().getSettings()

        switch (evt.getActionButtonId()) {
            case 153:
                settings.setRunToggled true
                break
            case 152:
                settings.setRunToggled false
                break
            case 150:
                settings.setAutoRetaliate true
                break
            case 151:
                settings.setAutoRetaliate false
                break
            case 21076:
                settings.setBrightness com.rs.entity.player.PlayerSettings.Brightness.DARK
                break
            case 24129:
                settings.setBrightness com.rs.entity.player.PlayerSettings.Brightness.NORMAL
                break
            case 24131:
                settings.setBrightness com.rs.entity.player.PlayerSettings.Brightness.BRIGHT
                break
            case 24133:
                settings.setBrightness com.rs.entity.player.PlayerSettings.Brightness.VERY_BRIGHT
                break
            case 24134:
                settings.setMouseButtons com.rs.entity.player.PlayerSettings.MouseButtons.TWO
                break
            case 24135:
                settings.setMouseButtons com.rs.entity.player.PlayerSettings.MouseButtons.ONE
                break
            case 24136:
                settings.setChatEffects true
                break
            case 24137:
                settings.setChatEffects false
                break
            case 3184:
                settings.setSplitPrivateChat true
                break
            case 3185:
                settings.setSplitPrivateChat false
                break
            case 49047:
                settings.setAcceptAid true
                break
            case 49046:
                settings.setAcceptAid false
                break
        }
    }

    @Override
    void tick() throws Exception {
    }

    @Override
    void onEnable(String pluginName) throws Exception {
        PluginEventDispatcher.register PluginEventDispatcher.ACTION_BUTTON_HANDLER_EVENT, pluginName
        PluginEventDispatcher.register PluginEventDispatcher.PLAYER_ON_LOGIN_EVENT, pluginName
    }

    @Override
    void onDisable() throws Exception {
    }
}
