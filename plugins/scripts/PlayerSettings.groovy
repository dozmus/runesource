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
import com.rs.plugin.event.ActionButtonEvent
import com.rs.plugin.event.PlayerLoggedInEvent
import com.rs.plugin.event.PlayerLoggedOutEvent
import com.rs.plugin.listener.ActionButtonListener
import com.rs.plugin.listener.PlayerConnectivityListener

class PlayerSettings implements PlayerConnectivityListener, ActionButtonListener {

    void logIn(PlayerLoggedInEvent evt) throws Exception {
        Player player = evt.getPlayer()
        com.rs.entity.player.PlayerSettings settings = player.getAttributes().getSettings()

        // Send initial setting states
        player.sendClientSetting(166, settings.getBrightness().settingValue())
        sendBooleanSetting(player, 170, settings.getMouseButtons() == com.rs.entity.player.PlayerSettings.MouseButtons.ONE)
        sendBooleanSetting(player, 171, settings.isChatEffects())
        sendBooleanSetting(player, 172, settings.isAutoRetaliate())
        sendBooleanSetting(player, 173, settings.isRunToggled())
        sendBooleanSetting(player, 287, settings.isSplitPrivateChat())
        sendBooleanSetting(player, 427, settings.isAcceptAid())

        // Send chat mode
        player.sendChatModes()
    }

    void logOut(PlayerLoggedOutEvent evt) {
    }

    void sendBooleanSetting(Player player, int settingId, boolean condition) {
        player.sendClientSetting(settingId, condition ? 1 : 0)
    }

    void actionButton(ActionButtonEvent evt) {
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
}
