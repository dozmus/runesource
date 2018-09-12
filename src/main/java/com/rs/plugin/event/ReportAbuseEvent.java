package com.rs.plugin.event;

import com.rs.entity.player.Player;
import com.rs.entity.player.infractions.ReportAbuse;

/**
 * A report abuse event.
 */
public class ReportAbuseEvent extends PlayerEvent {

    private final ReportAbuse reportAbuse;

    public ReportAbuseEvent(Player player, ReportAbuse reportAbuse) {
        super(player);
        this.reportAbuse = reportAbuse;
    }

    public ReportAbuse getReportAbuse() {
        return reportAbuse;
    }
}
