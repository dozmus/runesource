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
import com.rs.plugin.Plugin
import com.rs.plugin.PluginEventDispatcher
import com.rs.plugin.event.ModifyChatModeEvent
import com.rs.plugin.event.ModifyFriendsListEvent
import com.rs.plugin.event.ModifyIgnoredListEvent
import com.rs.plugin.event.PlayerLoggedOnEvent
import com.rs.plugin.event.PlayerLoggedOutEvent
import com.rs.plugin.event.PrivateMessageEvent
import com.rs.util.Misc

class PrivateMessaging extends Plugin {

    // TODO eliminate magic numbers in friends list status and chat modes values
    private static final int MAX_FRIENDS_LIST = 100
    private static final int MAX_IGNORED_LIST = 100
    private int MESSAGE_COUNTER = 1_000_000 * Math.random()

    @Override
    void tick() throws Exception {
    }

    void onLogin(PlayerLoggedOnEvent evt) throws Exception {
        // Update this player
        Player player = evt.getPlayer()
        long playerName = player.getUsername()

        player.sendFriendsListStatus(1) // status: connecting

        // Send friends
        player.getAttributes().getFriends().each { Map.Entry<Long, String> entry ->
            try {
                Player other = WorldHandler.getInstance().getPlayer(entry.value)
                PlayerSettings otherSettings = other.getAttributes().getSettings()
                boolean shownAsOffline = other.getAttributes().isIgnored(playerName) || otherSettings.getPrivateChatMode() == 2 || (otherSettings.getPrivateChatMode() == 1 && !other.getAttributes().isFriend(player.getUsername()))
                player.sendAddFriend(entry.key, shownAsOffline ? 0 : 10)
            } catch (IndexOutOfBoundsException ex) {
                player.sendAddFriend(entry.key, 0)
            }
        }

        // Send ignores
        player.sendAddIgnores(player.getAttributes().getIgnored().keySet())

        player.sendFriendsListStatus(2) // status: connected

        // Update other players
        updateOthersForPlayer(player, player.getAttributes().getSettings().getPrivateChatMode())
    }

    void onLogout(PlayerLoggedOutEvent evt) throws Exception {
        long playerName = evt.getPlayer().getUsername()

        // Update other players
        WorldHandler.getInstance().getPlayers().each { other ->
            if (other == null || other.getConnectionStage() != Client.ConnectionStage.LOGGED_IN || other == evt.getPlayer())
                return

            if (other.getAttributes().isFriend(playerName)) {
                other.sendAddFriend(playerName, 0)
            }
        }
    }

    void onAddIgnore(ModifyIgnoredListEvent evt) throws Exception {
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
                other.sendAddFriend(playerName, 0)
            }
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    void onRemoveIgnore(ModifyIgnoredListEvent evt) throws Exception {
        Player player = evt.getPlayer()
        PlayerSettings playerSettings = player.getAttributes().getSettings()
        long playerName = player.getUsername()

        player.getAttributes().removeIgnored(evt.getTarget())

        try {
            Player other = WorldHandler.getInstance().getPlayer(Misc.decodeBase37(evt.getTarget()))

            if (other.getAttributes().isFriend(playerName)) {
                boolean shownAsOffline = playerSettings.getPrivateChatMode() == 2 || (playerSettings.getPrivateChatMode() == 1 && !player.getAttributes().isFriend(player.getUsername()))
                other.sendAddFriend(playerName, shownAsOffline ? 0 : 10)
            }
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    void onAddFriend(ModifyFriendsListEvent evt) throws Exception {
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
            PlayerSettings otherSettings = other.getAttributes().getSettings()
            boolean shownAsOffline = other.getAttributes().isIgnored(playerName) || otherSettings.getPrivateChatMode() == 2 || (otherSettings.getPrivateChatMode() == 1 && !other.getAttributes().isFriend(player.getUsername()))
            player.sendAddFriend(evt.getTarget(), shownAsOffline ? 0 : 10)
        } catch (IndexOutOfBoundsException ex) {
            player.sendAddFriend(evt.getTarget(), 0)
        }
    }

    void onRemoveFriend(ModifyFriendsListEvent evt) throws Exception {
        Player player = evt.getPlayer()
        player.getAttributes().removeFriend(evt.getTarget())

        try {
            Player other = WorldHandler.getInstance().getPlayer(Misc.decodeBase37(evt.getTarget()))
            PlayerSettings settings = player.getAttributes().getSettings()

            if (settings.getPrivateChatMode() >= 1)
                other.sendAddFriend(player.getUsername(), 0)
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    void onPrivateMessage(PrivateMessageEvent evt) throws Exception {
        try {
            Player player = evt.getPlayer()
            PlayerAttributes attributes = player.getAttributes()
            Player other = WorldHandler.getInstance().getPlayer(Misc.decodeBase37(evt.getTarget()))

            // Check chat mode
            PlayerSettings settings = attributes.getSettings()

            if (settings.getPrivateChatMode() == 2) { // Offline
                if (attributes.isFriend(other.getUsername())) {
                    settings.setPrivateChatMode(1)
                } else {
                    settings.setPrivateChatMode(0)
                }
                player.sendChatModes()
                updateOthersForPlayer(player, settings.getPrivateChatMode())
            } else if (settings.getPrivateChatMode() == 1) { // Friends
                if (!attributes.isFriend(other.getUsername())) {
                    settings.setPrivateChatMode(0)
                    player.sendChatModes()
                    updateOthersForPlayer(player, settings.getPrivateChatMode())
                }
            }

            // Send message
            other.sendPrivateMessage(player.getUsername(), MESSAGE_COUNTER++, attributes.getPrivilege(), evt.getText())
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage("This player is not online.")
        }
    }

    void onModifyChatMode(ModifyChatModeEvent evt) throws Exception {
        int oldPrivateChatMode = evt.getPlayer().getAttributes().getSettings().getPrivateChatMode()
        int newPrivateChatMode = evt.getPrivateChatMode()

        // Check if a change was made
        if (newPrivateChatMode == oldPrivateChatMode)
            return

        // Update for all other players
        updateOthersForPlayer(evt.getPlayer(), newPrivateChatMode)
    }

    static void updateOthersForPlayer(Player player, int newPrivateChatMode) {
        WorldHandler.getInstance().getPlayers().each { other ->
            if (other == null || other.getConnectionStage() != Client.ConnectionStage.LOGGED_IN)
                return

            if (other.getAttributes().isFriend(player.getUsername())) {
                int worldNo = 10

                if (newPrivateChatMode == 2) { // Offline
                    worldNo = 0
                } else if (newPrivateChatMode == 1) { // Friends
                    worldNo = (player.getAttributes().isFriend(other.getUsername())) ? 10 : 0
                }
                other.sendAddFriend(player.getUsername(), worldNo)
            }
        }
    }

    @Override
    void onEnable(String pluginName) throws Exception {
        PluginEventDispatcher.register PluginEventDispatcher.PLAYER_ON_LOGIN_EVENT, pluginName
        PluginEventDispatcher.register PluginEventDispatcher.PLAYER_ON_LOGOUT_EVENT, pluginName
        PluginEventDispatcher.register PluginEventDispatcher.ADD_FRIEND_EVENT, pluginName
        PluginEventDispatcher.register PluginEventDispatcher.REMOVE_FRIEND_EVENT, pluginName
        PluginEventDispatcher.register PluginEventDispatcher.ADD_IGNORE_EVENT, pluginName
        PluginEventDispatcher.register PluginEventDispatcher.REMOVE_IGNORE_EVENT, pluginName
        PluginEventDispatcher.register PluginEventDispatcher.PRIVATE_MESSAGE_EVENT, pluginName
        PluginEventDispatcher.register PluginEventDispatcher.MODIFY_CHAT_MODE_EVENT, pluginName
    }

    @Override
    void onDisable() throws Exception {

    }
}
