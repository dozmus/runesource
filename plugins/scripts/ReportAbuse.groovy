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
import com.rs.entity.player.Player
import com.rs.entity.player.infractions.ReportAbuseRule
import com.rs.plugin.PluginHandler
import com.rs.plugin.event.ReportAbuseEvent
import com.rs.plugin.listener.ReportAbuseListener

import java.text.DateFormat
import java.text.SimpleDateFormat

class ReportAbuse implements ReportAbuseListener {

    File logFile = new File("./data/logs/report-abuse.log")
    DateFormat dateFormat = new SimpleDateFormat(Server.getInstance().getSettings().getDateFormat())

    void reportAbuse(ReportAbuseEvent e) {
        Player player = e.getPlayer()
        com.rs.entity.player.infractions.ReportAbuse abuse = e.getReportAbuse()
        boolean muted = abuse.muteFor48Hours

        // Mute using command - should remove this once we allow plugins to interact with each other
        if (muted) {
            Calendar expiration = Calendar.getInstance()
            expiration.setTime(new Date())
            expiration.add(Calendar.HOUR_OF_DAY, 48)
            PluginHandler.dispatchCommand(e.getPlayer(), "mute", dateFormat.format(expiration.getTime()))
        }

        log(player.getAttributes().getUsername(), abuse.username, abuse.rule, muted)
        player.setCurrentInterfaceId(-1)
    }

    void log(String author, String target, ReportAbuseRule rule, boolean muted) {
        String appDate = dateFormat.format(new Date())
        String log = "[$appDate]: [$author -> $target] $rule muted=$muted"

        FileWriter writer = new FileWriter(logFile, true)
        writer.write(log + System.lineSeparator())
        writer.flush()
        writer.close()
    }
}
