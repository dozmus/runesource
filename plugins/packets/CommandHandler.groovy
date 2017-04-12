import com.rs.entity.Position
import com.rs.entity.player.Player
import com.rs.entity.player.PlayerAttributes
import com.rs.entity.player.obj.Animation
import com.rs.entity.player.obj.Graphic
import com.rs.plugin.Plugin
import com.rs.plugin.PluginEventDispatcher
import com.rs.plugin.event.CommandEvent

class CommandHandler extends Plugin {

    void onCommand(CommandEvent evt) {
        Player player = evt.getPlayer()
        PlayerAttributes attributes = player.getAttributes()
        String commandName = evt.getCommandName()
        String[] args = evt.getArgs()

        if (commandName == "fchat") {
            player.setForceChatText args[0]
            player.setForceChatUpdateRequired true
            player.setUpdateRequired true
        }

        if (commandName == "gfx") {
            if (args.length < 2)
                return
            player.startGraphic new Graphic(args[0].toInteger(), args[1].toInteger())
        }

        if (commandName == "anim") {
            if (args.length < 2)
                return
            player.startAnimation new Animation(args[0].toInteger(), args[1].toInteger())
        }

        if (commandName == "energy") {
            if (args.length < 1)
                return
            player.getAttributes().setRunEnergy args[0].toInteger()
            player.sendRunEnergy()
        }

        if (commandName == "master") {
            for (int i = 0; i < attributes.getSkills().length; i++) {
                attributes.getSkills()[i] = 99
                attributes.getExperience()[i] = 14000000
            }
            player.sendSkills()
        }

        if (commandName == "noob") {
            for (int i = 0; i < attributes.getSkills().length; i++) {
                attributes.getSkills()[i] = 1
                attributes.getExperience()[i] = 0
            }
            player.sendSkills()
        }

        if (commandName == "empty") {
            attributes.emptyInventory player
        }

        if (commandName == "pickup" || commandName == "item") {
            int id = args[0].toInteger()
            int amount = args.length > 1 ? args[1].toInteger() : 1
            attributes.addInventoryItem id, amount, player
            player.sendInventory()
        }

        if (commandName == "tele") {
            if (args.length < 2)
                return
            int x = args[0].toInteger()
            int y = args[1].toInteger()
            int z = args.length > 2 ? args[2].toInteger() : player.getPosition().getZ()
            player.teleport new Position(x, y, z)
        }

        if (commandName == "mypos") {
            player.sendMessage "You are at: ${player.getPosition()}"
        }
    }

    @Override
    void tick() throws Exception {
    }

    @Override
    void onEnable(String pluginName) throws Exception {
        PluginEventDispatcher.registerEvent PluginEventDispatcher.COMMAND_HANDLER_EVENT, pluginName
    }

    @Override
    void onDisable() throws Exception {
    }
}