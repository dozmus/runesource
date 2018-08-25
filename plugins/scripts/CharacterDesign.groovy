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

class CharacterDesign implements PlayerConnectivityListener, ActionButtonListener {

    void actionButton(ActionButtonEvent evt) {
        Player player = evt.getPlayer()

        if (evt.getActionButtonId() == 14067) { // Design character interface accept button
            player.sendClearScreen()
            player.setCurrentInterfaceId(-1)
        }
    }

    void logIn(PlayerLoggedInEvent evt) {
        Player player = evt.getPlayer()

        if (evt.isNewPlayer()) {
            player.sendInterface 3559
            player.setCurrentInterfaceId 3559
        }
    }

    void logOut(PlayerLoggedOutEvent evt) {
    }
}
