import com.rs.WorldHandler
import com.rs.entity.player.Client
import com.rs.entity.player.Player
import com.rs.entity.player.PlayerAttributes
import com.rs.plugin.Plugin
import com.rs.plugin.PluginBridge

class PrivateMessaging extends Plugin {

    private int MESSAGE_COUNTER = 1_000_000 * Math.random()

    @Override
    void tick() throws Exception {
    }

    void onLogin(Player player) throws Exception {
        // Update this player
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

    void onLogout(Player player) throws Exception {
        // Update other players
        long playerName = PlayerAttributes.nameToLong(player.getAttributes().getUsername())

        WorldHandler.getPlayers().each { other ->
            if (other == null || other.getStage() != Client.Stage.LOGGED_IN)
                return

            if (other.getAttributes().isFriend(playerName)) {
                other.sendAddFriend(playerName, 0)
            }
        }
    }

    void onAddFriend(Player player, long name) throws Exception {
        player.getAttributes().addFriend(name)

        if (WorldHandler.isPlayerOnline(PlayerAttributes.nameForLong(name))) {
            player.sendAddFriend(name, 10)
        }
    }

    void onRemoveFriend(Player player, long name) throws Exception {
        player.getAttributes().removeFriend(name)
    }

    void onPrivateMessage(Player player, long name, byte[] text) throws Exception {
        try {
            Player other = WorldHandler.getPlayer(PlayerAttributes.nameForLong(name))
            other.sendPrivateMessage(PlayerAttributes.nameToLong(player.getAttributes().getUsername()),
                    MESSAGE_COUNTER++, player.getAttributes().getStaffRights(), text)
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage("This player is not online.")
        }
    }

    @Override
    void onEnable(String pluginName) throws Exception {
        PluginBridge.registerEvent PluginBridge.PLAYER_ON_LOGIN_EVENT, pluginName
        PluginBridge.registerEvent PluginBridge.PLAYER_ON_LOGOUT_EVENT, pluginName
        PluginBridge.registerEvent PluginBridge.ADD_FRIEND_EVENT, pluginName
        PluginBridge.registerEvent PluginBridge.REMOVE_FRIEND_EVENT, pluginName
        PluginBridge.registerEvent PluginBridge.PRIVATE_MESSAGE_EVENT, pluginName
    }

    @Override
    void onDisable() throws Exception {

    }
}