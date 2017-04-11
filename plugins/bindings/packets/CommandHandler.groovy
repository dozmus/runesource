import com.rs.entity.Position
import com.rs.entity.player.Player
import com.rs.entity.player.PlayerAttributes
import com.rs.entity.player.obj.Animation
import com.rs.entity.player.obj.Graphic
import com.rs.plugin.Plugin
import com.rs.plugin.PluginBridge

class CommandHandler extends Plugin {

    void handle(Player player, String keyword, String[] args) {
        PlayerAttributes attributes = player.getAttributes()

        if (keyword == "fchat") {
            player.setForceChatText args[0]
            player.setForceChatUpdateRequired true
            player.setUpdateRequired true
        }

        if (keyword == "gfx") {
            if (args.length < 2)
                return
            player.startGraphic new Graphic(args[0].toInteger(), args[1].toInteger())
        }

        if (keyword == "anim") {
            if (args.length < 2)
                return
            player.startAnimation new Animation(args[0].toInteger(), args[1].toInteger())
        }

        if (keyword == "energy") {
            player.getAttributes().setRunEnergy args[0].toInteger()
            player.sendRunEnergy()
        }

        if (keyword == "master") {
            for (int i = 0; i < attributes.getSkills().length; i++) {
                attributes.getSkills()[i] = 99
                attributes.getExperience()[i] = 14000000
            }
            player.sendSkills()
        }

        if (keyword == "noob") {
            for (int i = 0; i < attributes.getSkills().length; i++) {
                attributes.getSkills()[i] = 1
                attributes.getExperience()[i] = 0
            }
            player.sendSkills()
        }

        if (keyword == "empty") {
            attributes.emptyInventory player
        }

        if (keyword == "pickup" || keyword == "item") {
            int id = args[0].toInteger()
            int amount = args.length > 1 ? args[1].toInteger() : 1
            attributes.addInventoryItem id, amount, player
            player.sendInventory()
        }

        if (keyword == "tele") {
            if (args.length < 2)
                return
            int x = args[0].toInteger()
            int y = args[1].toInteger()
            int z = args.length > 2 ? args[2].toInteger() : player.getPosition().getZ()
            player.teleport new Position(x, y, z)
        }

        if (keyword == "mypos") {
            player.sendMessage "You are at: {$player.getPosition()}"
        }
    }

    @Override
    void tick() throws Exception {
    }

    @Override
    void onEnable(String pluginName) throws Exception {
        PluginBridge.registerBinding(PluginBridge.COMMAND_HANDLER_BINDING_KEY, pluginName)
    }

    @Override
    void onDisable() throws Exception {
    }
}