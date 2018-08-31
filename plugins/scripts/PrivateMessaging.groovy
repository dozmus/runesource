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
import com.rs.WorldHandler
import com.rs.entity.player.Client
import com.rs.entity.player.Player
import com.rs.entity.player.PlayerAttributes
import com.rs.entity.player.PlayerSettings
import com.rs.plugin.event.ModifyChatModeEvent
import com.rs.plugin.event.ModifyPlayerListEvent

import com.rs.plugin.event.PlayerLoggedInEvent
import com.rs.plugin.event.PlayerLoggedOutEvent
import com.rs.plugin.event.PrivateMessageEvent
import com.rs.plugin.event.PublicMessageEvent
import com.rs.plugin.listener.MessageConfigListener
import com.rs.plugin.listener.MessageListener
import com.rs.plugin.listener.PlayerConnectivityListener
import com.rs.util.Misc

class PrivateMessaging implements PlayerConnectivityListener, MessageConfigListener, MessageListener {

    private static final int WORLD_1 = 10
    private static final int WORLD_OFFLINE = 0
    private static final int PRIVATE_CHAT_ONLINE = 0
    private static final int PRIVATE_CHAT_FRIENDS = 1
    private static final int PRIVATE_CHAT_OFFLINE = 2
    private static final int MAX_FRIENDS_LIST = 100
    private static final int MAX_IGNORED_LIST = 100
    private static final int FRIENDS_LIST_CONNECTING = 1
    private static final int FRIENDS_LIST_CONNECTED = 2
    private int MESSAGE_COUNTER = 1_000_000 * Math.random()

    void logIn(PlayerLoggedInEvent evt) throws Exception {
        // Update this player
        Player player = evt.getPlayer()

        player.sendFriendsListStatus(FRIENDS_LIST_CONNECTING) // status: connecting

        // Send friends
        player.getAttributes().getFriends().each { Map.Entry<Long, String> entry ->
            try {
                Player other = WorldHandler.getInstance().getPlayer(entry.value)
                sendAddFriend(player, other)
            } catch (IndexOutOfBoundsException ex) {
                player.sendAddFriend(entry.key, WORLD_OFFLINE)
            }
        }

        // Send ignores
        player.sendAddIgnores(player.getAttributes().getIgnored().keySet())

        player.sendFriendsListStatus(FRIENDS_LIST_CONNECTED) // status: connected

        // Update other players friends lists
        updateOthersForPlayer(player, player.getAttributes().getSettings().getPrivateChatMode())
    }

    void logOut(PlayerLoggedOutEvent evt) throws Exception {
        long playerName = evt.getPlayer().getUsername()

        // Update other players
        WorldHandler.getInstance().getPlayers().each { other ->
            if (other == null || other.getConnectionStage() != Client.ConnectionStage.LOGGED_IN || other == evt.getPlayer())
                return

            if (other.getAttributes().isFriend(playerName)) {
                other.sendAddFriend(playerName, WORLD_OFFLINE)
            }
        }
    }

    void addIgnore(ModifyPlayerListEvent evt) throws Exception {
        Player player = evt.getPlayer()
        long playerName = player.getUsername()
        String otherPlayerName = Misc.decodeBase37(evt.getTarget())

        // Ignore if trying to add self
        if (playerName == evt.getTarget())
            return

        // Ignore if target on friends list
        if (player.getAttributes().isFriend(evt.getTarget()))
            return

        // Ignore if list full
        if (player.getAttributes().getIgnored().size() >= MAX_IGNORED_LIST)
            return

        // Ignore if name invalid
        if (!Misc.validateUsername(otherPlayerName))
            return
        
        player.getAttributes().addIgnored(evt.getTarget())

        try {
            Player other = WorldHandler.getInstance().getPlayer(otherPlayerName)

            if (other.getAttributes().isFriend(playerName)) {
                other.sendAddFriend(playerName, WORLD_OFFLINE)
            }
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    void removeIgnore(ModifyPlayerListEvent evt) throws Exception {
        Player player = evt.getPlayer()
        long playerName = player.getUsername()
        player.getAttributes().removeIgnored(evt.getTarget())

        try {
            Player other = WorldHandler.getInstance().getPlayer(Misc.decodeBase37(evt.getTarget()))

            if (other.getAttributes().isFriend(playerName)) {
                sendAddFriend(player, other)
            }
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    void addFriend(ModifyPlayerListEvent evt) throws Exception {
        Player player = evt.getPlayer()
        long playerName = player.getUsername()
        String otherPlayerName = Misc.decodeBase37(evt.getTarget())

        // Ignore if trying to add self
        if (playerName == evt.getTarget())
            return

        // Ignore if target on ignored list
        if (player.getAttributes().isIgnored(evt.getTarget()))
            return

        // Ignore if list full
        if (player.getAttributes().getFriends().size() >= MAX_FRIENDS_LIST)
            return

        // Ignore if name invalid
        if (!Misc.validateUsername(otherPlayerName))
            return

        // Regular logic
        player.getAttributes().addFriend(evt.getTarget())

        try {
            Player other = WorldHandler.getInstance().getPlayer(otherPlayerName)
            sendAddFriend(player, other)

            if (other.getAttributes().isFriend(playerName)) {
                sendAddFriend(other, player)
            }
        } catch (IndexOutOfBoundsException ex) {
            player.sendAddFriend(evt.getTarget(), WORLD_OFFLINE)
        }
    }

    void removeFriend(ModifyPlayerListEvent evt) throws Exception {
        Player player = evt.getPlayer()
        player.getAttributes().removeFriend(evt.getTarget())

        try {
            Player other = WorldHandler.getInstance().getPlayer(Misc.decodeBase37(evt.getTarget()))
            PlayerSettings settings = player.getAttributes().getSettings()

            if (settings.getPrivateChatMode() >= PRIVATE_CHAT_FRIENDS) {
                other.sendAddFriend(player.getUsername(), WORLD_OFFLINE)
            }
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    void privateMessage(PrivateMessageEvent evt) throws Exception {
        Player player = evt.getPlayer()
        PlayerAttributes attributes = player.getAttributes()
        PlayerSettings settings = attributes.getSettings()

        if (attributes.getInfractions().isMuted()) {
            return
        }

        try {
            Player other = WorldHandler.getInstance().getPlayer(Misc.decodeBase37(evt.getTarget()))
            PlayerAttributes otherAttributes = other.getAttributes()
            PlayerSettings otherSettings = otherAttributes.getSettings()

            // Check our chat mode
            if (settings.getPrivateChatMode() == PRIVATE_CHAT_OFFLINE) { // Private chat offline
                boolean mutualFriends = attributes.isFriend(other.getUsername()) && otherAttributes.isFriend(player.getUsername())
                settings.setPrivateChatMode(mutualFriends ? PRIVATE_CHAT_FRIENDS : PRIVATE_CHAT_ONLINE)
                player.sendChatModes()
                updateOthersForPlayer(player, settings.getPrivateChatMode())
            } else if (settings.getPrivateChatMode() == PRIVATE_CHAT_FRIENDS) { // Private chat friends
                if (!otherAttributes.isFriend(player.getUsername())) {
                    settings.setPrivateChatMode(PRIVATE_CHAT_ONLINE)
                    player.sendChatModes()
                    updateOthersForPlayer(player, settings.getPrivateChatMode())
                }
            }

            // Send message - the client will filter it out depending on the other player's chat mode
            other.sendPrivateMessage(player.getUsername(), MESSAGE_COUNTER++, attributes.getPrivilege(), evt.getText())
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage("This player is not online.")
        }
    }

    void publicMessage(PublicMessageEvent e) {
    }

    void modifyChatMode(ModifyChatModeEvent evt) throws Exception {
        int oldPrivateChatMode = evt.getPlayer().getAttributes().getSettings().getPrivateChatMode()
        int newPrivateChatMode = evt.getPrivateChatMode()

        // Check if a change was made in private chat mode
        if (newPrivateChatMode == oldPrivateChatMode)
            return

        // Update for all other players
        updateOthersForPlayer(evt.getPlayer(), newPrivateChatMode)
    }

    /**
     * Sends other to the player's friends list with the appropriate world number.
     */
    static void sendAddFriend(Player player, Player other) {
        PlayerAttributes otherAttributes = other.getAttributes()
        int otherPrivateChatMode = otherAttributes.getSettings().getPrivateChatMode()

        boolean isIgnored = otherAttributes.isIgnored(player.getUsername())
        boolean offlineMode = otherPrivateChatMode == PRIVATE_CHAT_OFFLINE
        boolean friendsMode = otherPrivateChatMode == PRIVATE_CHAT_FRIENDS && !otherAttributes.isFriend(player.getUsername())
        boolean shownAsOffline = isIgnored || offlineMode || friendsMode
        int worldNo = shownAsOffline ? WORLD_OFFLINE : WORLD_1
        player.sendAddFriend(other.getUsername(), worldNo)
    }

    /**
     * Updates every other player's friends list if they contain the player, according to their new private chat mode.
     */
    static void updateOthersForPlayer(Player player, int newPrivateChatMode) {
        WorldHandler.getInstance().getPlayers().each { other ->
            if (other == null || other.getConnectionStage() != Client.ConnectionStage.LOGGED_IN)
                return

            if (other.getAttributes().isFriend(player.getUsername())) {
                int worldNo = WORLD_1

                if (newPrivateChatMode == PRIVATE_CHAT_OFFLINE) { // Offline
                    worldNo = WORLD_OFFLINE
                } else if (newPrivateChatMode == PRIVATE_CHAT_FRIENDS) { // Friends
                    worldNo = (player.getAttributes().isFriend(other.getUsername())) ? WORLD_1 : WORLD_OFFLINE
                }
                other.sendAddFriend(player.getUsername(), worldNo)
            }
        }
    }
}
