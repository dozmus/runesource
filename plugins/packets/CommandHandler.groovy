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
import com.rs.entity.Position
import com.rs.entity.npc.Npc
import com.rs.entity.player.Player
import com.rs.entity.player.PlayerAttributes
import com.rs.entity.action.Animation
import com.rs.entity.action.Graphics
import com.rs.plugin.event.CommandEvent
import com.rs.plugin.listener.CommandListener

class CommandHandler implements CommandListener {

    void command(CommandEvent evt) {
        Player player = evt.getPlayer()
        PlayerAttributes attributes = player.getAttributes()
        String commandName = evt.getCommandName()
        String[] args = evt.getArgs()

        if (commandName == "fchat") {
            if (args.length < 1) {
                player.sendMessage("Syntax: ::fchat [text]")
                return
            }
            player.setForceChatText args.join(" ")
            player.getUpdateContext().setForcedChatUpdateRequired()
        }

        if (commandName == "gfx") {
            if (args.length < 2) {
                player.sendMessage("Syntax: ::gfx [id] [delay]")
                return
            }
            player.startGraphic new Graphics(args[0].toInteger(), args[1].toInteger())
        }

        if (commandName == "anim") {
            if (args.length < 2) {
                player.sendMessage("Syntax: ::anim [id] [delay]")
                return
            }
            player.startAnimation new Animation(args[0].toInteger(), args[1].toInteger())
        }

        if (commandName == "interface") {
            if (args.length < 1) {
                player.sendMessage("Syntax: ::interface [id]")
                return
            }
            player.sendInterface args[0].toInteger()
            player.setCurrentInterfaceId args[0].toInteger()
        }

        if (commandName == "closeinterface") {
            player.sendClearScreen()
            player.setCurrentInterfaceId(-1)
        }

        if (commandName == "energy") {
            if (args.length < 1) {
                player.sendMessage("Syntax: ::energy [amount]")
                return
            }
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
            if (args.length < 1) {
                player.sendMessage("Syntax: ::" + commandName + " [id] [amount]")
                return
            }
            int id = args[0].toInteger()
            int amount = args.length > 1 ? args[1].toInteger() : 1
            attributes.addInventoryItem id, amount, player
            player.sendInventory()
        }

        if (commandName == "tele") {
            if (args.length < 2) {
                player.sendMessage("Syntax: ::tele [x] [y] [z]")
                return
            }
            int x = args[0].toInteger()
            int y = args[1].toInteger()
            int z = args.length > 2 ? args[2].toInteger() : player.getPosition().getZ()
            player.teleport new Position(x, y, z)
        }

        if (commandName == "npc") {
            if (args.length < 1) {
                player.sendMessage("Syntax: ::npc [id] [x] [y] [z]")
                return
            }
            Npc npc = new Npc(args[0].toInteger())
            npc.getPosition().setAs(player.getPosition())

            if (args.length >= 3) {
                npc.getPosition().setX(args[1].toInteger())
                npc.getPosition().setY(args[2].toInteger())
            }

            if (args.length >= 4) {
                npc.getPosition().setZ(args[3].toInteger())
            }
            WorldHandler.getInstance().register(npc)
        }

        if (commandName == "mypos") {
            player.sendMessage "You are at: ${player.getPosition()}"
        }
    }
}
