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
