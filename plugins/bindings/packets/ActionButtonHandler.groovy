import com.rs.entity.player.Player
import com.rs.plugin.Plugin
import com.rs.plugin.PluginBridge

class ActionButtonHandler extends Plugin {

    void handle(Player player, int actionButtonId) {
        switch (actionButtonId) {
            case 9154:
                player.sendLogout();
                break;
            case 153:
                player.getMovementHandler().setRunToggled(true);
                break;
            case 152:
                player.getMovementHandler().setRunToggled(false);
                break;
            default:
                System.out.println("Unhandled button: " + actionButtonId);
                break;
        }
    }

    @Override
    void cycle() throws Exception {
    }

    @Override
    void onEnable(String pluginName) throws Exception {
        PluginBridge.registerBinding(PluginBridge.ACTION_BUTTON_HANDLER_BINDING_KEY, pluginName);
    }

    @Override
    void onDisable() throws Exception {
    }
}