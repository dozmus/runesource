import com.rs.WorldHandler
import com.rs.entity.player.Client
import com.rs.entity.player.Player
import com.rs.entity.player.PlayerAttributes
import com.rs.plugin.Plugin
import com.rs.plugin.PluginEventDispatcher
import com.rs.plugin.event.ModifyFriendsListEvent
import com.rs.plugin.event.PlayerLoggedOutEvent
import com.rs.plugin.event.PlayerLoggedOnEvent
import com.rs.plugin.event.PrivateMessageEvent

class PrivateMessaging extends Plugin {

    private int MESSAGE_COUNTER = 1_000_000 * Math.random()

    @Override
    void tick() throws Exception {
    }

    void onLogin(PlayerLoggedOnEvent evt) throws Exception {
        // Update this player
        Player player = evt.getPlayer()
        long playerName = PlayerAttributes.nameToLong(player.getAttributes().getUsername())
        player.sendFriendsListStatus(1) // status: connecting

        player.getAttributes().getFriends().each { Map.Entry<Long, String> entry ->
            player.sendAddFriend(entry.key, WorldHandler.isPlayerOnline(entry.value) ? 10 : 0)
        }

        // TODO send ignores

        player.sendFriendsListStatus(2) // status: connected

        // Update other players
        WorldHandler.getPlayers().each { other ->
            if (other == null || other.getStage() != Client.Stage.LOGGED_IN)
                return

            if (other.getAttributes().isFriend(playerName)) {
                other.sendAddFriend(playerName, 10)
            }
        }
    }

    void onLogout(PlayerLoggedOutEvent evt) throws Exception {
        // Update other players
        long playerName = PlayerAttributes.nameToLong(evt.getPlayer().getAttributes().getUsername())

        WorldHandler.getPlayers().each { other ->
            if (other == null || other.getStage() != Client.Stage.LOGGED_IN)
                return

            if (other.getAttributes().isFriend(playerName)) {
                other.sendAddFriend(playerName, 0)
            }
        }
    }

    void onAddFriend(ModifyFriendsListEvent evt) throws Exception {
        // Ignore if trying to add self
        if (PlayerAttributes.nameToLong(evt.getPlayer().getAttributes().getUsername()) == evt.getTarget())
            return

        // Regular logic
        evt.getPlayer().getAttributes().addFriend(evt.getTarget())

        if (WorldHandler.isPlayerOnline(PlayerAttributes.nameForLong(evt.getTarget()))) {
            evt.getPlayer().sendAddFriend(evt.getTarget(), 10)
        }
    }

    void onRemoveFriend(ModifyFriendsListEvent evt) throws Exception {
        evt.getPlayer().getAttributes().removeFriend(evt.getTarget())
    }

    void onPrivateMessage(PrivateMessageEvent evt) throws Exception {
        try {
            Player player = evt.getPlayer()
            Player other = WorldHandler.getPlayer(PlayerAttributes.nameForLong(evt.getTarget()))
            other.sendPrivateMessage(PlayerAttributes.nameToLong(player.getAttributes().getUsername()),
                    MESSAGE_COUNTER++, player.getAttributes().getStaffRights(), evt.getText())
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage("This player is not online.")
        }
    }

    @Override
    void onEnable(String pluginName) throws Exception {
        PluginEventDispatcher.registerEvent PluginEventDispatcher.PLAYER_ON_LOGIN_EVENT, pluginName
        PluginEventDispatcher.registerEvent PluginEventDispatcher.PLAYER_ON_LOGOUT_EVENT, pluginName
        PluginEventDispatcher.registerEvent PluginEventDispatcher.ADD_FRIEND_EVENT, pluginName
        PluginEventDispatcher.registerEvent PluginEventDispatcher.REMOVE_FRIEND_EVENT, pluginName
        PluginEventDispatcher.registerEvent PluginEventDispatcher.PRIVATE_MESSAGE_EVENT, pluginName
    }

    @Override
    void onDisable() throws Exception {

    }
}