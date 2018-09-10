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
import com.rs.Server
import com.rs.WorldHandler
import com.rs.entity.player.Player
import com.rs.entity.player.PlayerAttributes

import com.rs.entity.player.infractions.PlayerInfractions

import com.rs.plugin.event.CommandEvent
import com.rs.plugin.event.PlayerLoggedInEvent
import com.rs.plugin.event.PlayerLoggedOutEvent
import com.rs.plugin.listener.CommandListener
import com.rs.plugin.listener.PlayerConnectivityListener

import java.text.DateFormat
import java.text.SimpleDateFormat

class Infractions implements PlayerConnectivityListener, CommandListener {

    File logFile = new File("./data/logs/infractions.log")
    DateFormat dateFormat = new SimpleDateFormat(Server.getInstance().getSettings().getDateFormat())
    PlayerInfractionCommand[] commands = [
            new PlayerInfractionCommand("mute", Player.Privilege.MODERATOR, true) {
                @Override
                void apply(Player target, PlayerInfractions infractions, Date expirationDate) {
                    infractions.setMuted(true)
                    infractions.setMuteExpirationDate(expirationDate)

                    if (target != null) {
                        target.sendMessage("You have been muted, it will expire: " + infractions.muteExpiration())
                    }
                }
            },
            new PlayerInfractionCommand("unmute", Player.Privilege.MODERATOR, false) {
                @Override
                void apply(Player target, PlayerInfractions infractions, Date expirationDate) {
                    infractions.setMuted(false)
                    infractions.setMuteExpirationDate(expirationDate)

                    if (target != null) {
                        target.sendMessage("You have been unmuted.")
                    }
                }
            },
            new PlayerInfractionCommand("ban", Player.Privilege.MODERATOR, true) {
                @Override
                void apply(Player target, PlayerInfractions infractions, Date expirationDate) {
                    infractions.setBanned(true)
                    infractions.setBanExpirationDate(expirationDate)

                    if (target != null) {
                        target.disconnect()
                    }
                }
            },
            new PlayerInfractionCommand("unban", Player.Privilege.MODERATOR, false) {
                @Override
                void apply(Player target, PlayerInfractions infractions, Date expirationDate) {
                    infractions.setBanned(false)
                    infractions.setBanExpirationDate(expirationDate)
                }
            }
    ]

    void logIn(PlayerLoggedInEvent evt) {
        Player player = evt.getPlayer()
        PlayerInfractions infractions = player.getAttributes().getInfractions()

        if (infractions.isMuted()) {
            player.sendMessage("Your mute will expire: " + infractions.muteExpiration())
        }
    }

    void logOut(PlayerLoggedOutEvent evt) {
    }

    void command(CommandEvent evt) {
        Player player = evt.getPlayer()
        PlayerAttributes attributes = player.getAttributes()
        String commandName = evt.getCommandName()
        String[] args = evt.getArgs()

        // Map commandName to PlayerInfractionCommand
        PlayerInfractionCommand cmd

        try {
            cmd = command(commandName)
        } catch (IndexOutOfBoundsException ignored) {
            return
        }

        // Validate args
        Date expirationDate

        if (cmd.isTimedInfraction) {
            if (args.length < 2) {
                player.sendMessage("Syntax: ::$commandName [username] [expiration date, or 'never']")
                return
            }
            String expirationDateStr = join(" ", args, 1)
            expirationDate = (expirationDateStr == "never") ? null : dateFormat.parse(expirationDateStr)
        } else {
            if (args.length < 1) {
                player.sendMessage("Syntax: ::$commandName [username]")
                return
            }
        }

        // Apply infraction
        String targetUsername = args[0]
        Date applicationDate = new Date()
        PlayerAttributes targetAttributes

        try { // check online players
            Player targetPlayer = WorldHandler.getInstance().getPlayer(targetUsername)
            targetAttributes = targetPlayer.getAttributes()
            infract(targetPlayer, targetAttributes, attributes, cmd, applicationDate, expirationDate)
        } catch (IndexOutOfBoundsException e) { // load from file
            try {
                targetAttributes = Server.getInstance().getPlayerFileHandler().load(targetUsername)
                infract(null, targetAttributes, attributes, cmd, applicationDate, expirationDate)
                Server.getInstance().getPlayerFileHandler().save(targetAttributes)
            } catch (Exception ex) {
                player.sendMessage("player with username '$username' does not exist")
            }
        }
    }

    void infract(Player targetPlayer, PlayerAttributes targetAttributes, PlayerAttributes authorAttributes,
                 PlayerInfractionCommand cmd, Date applicationDate, Date expirationDate) {
        // Check comparative privileges
        if (targetAttributes.getPrivilege().gte(authorAttributes.getPrivilege())) {
            player.sendMessage("You cannot infract another player with the same, or greater privilege than you")
            return
        }

        // Apply infraction
        PlayerInfractions infractions = targetAttributes.getInfractions()
        cmd.apply(targetPlayer, infractions, expirationDate)

        // Log
        log(targetAttributes.getUsername(), authorAttributes.getUsername(), cmd, applicationDate, expirationDate)
    }

    void log(String target, String author, PlayerInfractionCommand cmd, Date applicationDate, Date expirationDate) {
        String appDate = dateFormat.format(applicationDate)
        String expData = expirationDate == null ? "never" : dateFormat.format(expirationDate)
        String operation = cmd.commandName
        String log = "[$appDate]: [$author -> $target] $operation expiry=$expData"

        FileWriter writer = new FileWriter(logFile, true)
        writer.write(log + System.lineSeparator())
        writer.flush()
        writer.close()
    }

    PlayerInfractionCommand command(String commandName) {
        for (PlayerInfractionCommand pic : commands) {
            if (pic.getCommandName().equalsIgnoreCase(commandName)) {
                return pic
            }
        }
        throw new IndexOutOfBoundsException()
    }

    String join(String delimeter, String[] collection, int startIdx) {
        StringBuilder builder = new StringBuilder()

        for (int i = startIdx; i < collection.length; i++) {
            builder.append(collection[i])

            if (i + 1 < collection.length)
                builder.append(delimeter)
        }

        return builder.toString()
    }

    abstract class PlayerInfractionCommand {

        String commandName
        Player.Privilege privilege
        boolean isTimedInfraction

        PlayerInfractionCommand(String commandName, Player.Privilege privilege, boolean isTimedInfraction) {
            this.commandName = commandName
            this.privilege = privilege
            this.isTimedInfraction = isTimedInfraction
        }

        abstract void apply(Player target, PlayerInfractions targetAttributes, Date expirationDate);
    }
}
