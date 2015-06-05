import com.rs.entity.Position
import com.rs.entity.player.Player
import com.rs.entity.player.PlayerAttributes
import com.rs.plugin.Plugin
import com.rs.plugin.PluginBridge

class CommandHandler extends Plugin {

    void handle(Player player, String keyword, String[] args) {
        PlayerAttributes attributes = player.getAttributes();

        if (keyword.equals("master")) {
            for (int i = 0; i < attributes.getSkills().length; i++) {
                attributes.getSkills()[i] = 99;
                attributes.getExperience()[i] = 200000000;
            }
            player.sendSkills();
        }

        if (keyword.equals("noob")) {
            for (int i = 0; i < attributes.getSkills().length; i++) {
                attributes.getSkills()[i] = 1;
                attributes.getExperience()[i] = 0;
            }
            player.sendSkills();
        }
        if (keyword.equals("empty")) {
            attributes.emptyInventory(this);
        }

        if (keyword.equals("pickup")) {
            int id = Integer.parseInt(args[0]);
            int amount = 1;
            if (args.length > 1) {
                amount = Integer.parseInt(args[1]);
            }
            attributes.addInventoryItem(id, amount, this);
            player.sendInventory();
        }

        if (keyword.equals("tele")) {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            player.teleport(new Position(x, y, player.getPosition().getZ()));
        }

        if (keyword.equals("mypos")) {
            player.sendMessage("You are at: " + player.getPosition());
        }
    }

    @Override
    void cycle() throws Exception {
    }

    @Override
    void onEnable(String pluginName) throws Exception {
        PluginBridge.registerBinding(PluginBridge.COMMAND_HANDLER_BINDING_KEY, pluginName);
    }

    @Override
    void onDisable() throws Exception {
    }
}