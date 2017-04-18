import com.rs.WorldHandler
import com.rs.entity.player.Client
import com.rs.entity.player.Player
import com.rs.plugin.Plugin
import com.rs.plugin.PluginEventDispatcher
import com.rs.plugin.event.ModifyFriendsListEvent
import com.rs.plugin.event.ModifyIgnoredListEvent
import com.rs.plugin.event.PlayerLoggedOnEvent
import com.rs.plugin.event.PlayerLoggedOutEvent
import com.rs.plugin.event.PrivateMessageEvent
import com.rs.util.Misc

class PrivateMessaging extends Plugin {

    private static final int MAX_FRIENDS_LIST = 200
    private static final int MAX_IGNORED_LIST = 200
    private int MESSAGE_COUNTER = 1_000_000 * Math.random()

    @Override
    void tick() throws Exception {
    }

    void onLogin(PlayerLoggedOnEvent evt) throws Exception {
        // Update this player
        Player player = evt.getPlayer()
        long playerName = Misc.encodeBase37(player.getAttributes().getUsername())

        player.sendFriendsListStatus(1) // status: connecting

        // Send friends
        player.getAttributes().getFriends().each { Map.Entry<Long, String> entry ->
            try {
                Player other = WorldHandler.getInstance().getPlayer(entry.value)
                player.sendAddFriend(entry.key, other.getAttributes().isIgnored(playerName) ? 0 : 10)
            } catch (IndexOutOfBoundsException ex) {
                player.sendAddFriend(entry.key, 0)
            }
        }

        // Send ignores
        player.sendAddIgnores(player.getAttributes().getIgnored().keySet())

        player.sendFriendsListStatus(2) // status: connected

        // Update other players
        WorldHandler.getInstance().getPlayers().each { other ->
            if (other == null || other.getStage() != Client.Stage.LOGGED_IN)
                return

            if (other.getAttributes().isFriend(playerName)) {
                other.sendAddFriend(playerName, 10)
            }
        }
    }

    void onLogout(PlayerLoggedOutEvent evt) throws Exception {
        // Update other players
        long playerName = Misc.encodeBase37(evt.getPlayer().getAttributes().getUsername())

        WorldHandler.getInstance().getPlayers().each { other ->
            if (other == null || other.getStage() != Client.Stage.LOGGED_IN)
                return

            if (other.getAttributes().isFriend(playerName)) {
                other.sendAddFriend(playerName, 0)
            }
        }
    }

    void onAddIgnore(ModifyIgnoredListEvent evt) throws Exception {
        Player player = evt.getPlayer()
        long playerName = Misc.encodeBase37(player.getAttributes().getUsername())
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
        long playerName = Misc.encodeBase37(player.getAttributes().getUsername())

        player.getAttributes().removeIgnored(evt.getTarget())

        try {
            Player other = WorldHandler.getInstance().getPlayer(Misc.decodeBase37(evt.getTarget()))

            if (other.getAttributes().isFriend(playerName)) {
                other.sendAddFriend(playerName, 10)
            }
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    void onAddFriend(ModifyFriendsListEvent evt) throws Exception {
        Player player = evt.getPlayer()
        long playerName = Misc.encodeBase37(player.getAttributes().getUsername())
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
            player.sendAddFriend(evt.getTarget(), other.getAttributes().isIgnored(playerName) ? 0 : 10)
        } catch (IndexOutOfBoundsException ex) {
            player.sendAddFriend(evt.getTarget(), 0)
        }
    }

    void onRemoveFriend(ModifyFriendsListEvent evt) throws Exception {
        evt.getPlayer().getAttributes().removeFriend(evt.getTarget())
    }

    void onPrivateMessage(PrivateMessageEvent evt) throws Exception {
        try {
            Player player = evt.getPlayer()
            Player other = WorldHandler.getInstance().getPlayer(Misc.decodeBase37(evt.getTarget()))

            if (!other.getAttributes().isIgnored(Misc.encodeBase37(player.getAttributes().getUsername()))) {
                other.sendPrivateMessage(Misc.encodeBase37(player.getAttributes().getUsername()),
                        MESSAGE_COUNTER++, player.getAttributes().getPrivilege(), evt.getText())
            }
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage("This player is not online.")
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
    }

    @Override
    void onDisable() throws Exception {

    }
}