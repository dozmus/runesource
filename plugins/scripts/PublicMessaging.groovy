import com.rs.entity.player.Player
import com.rs.plugin.event.PrivateMessageEvent
import com.rs.plugin.event.PublicMessageEvent
import com.rs.plugin.listener.MessageListener

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

class PublicMessaging implements MessageListener {

    void publicMessage(PublicMessageEvent e) {
        Player player = e.getPlayer()
        player.setPublicChat e.getPublicChat()
        player.getUpdateContext().setPublicChatUpdateRequired()
    }

    void privateMessage(PrivateMessageEvent e) {
    }
}
