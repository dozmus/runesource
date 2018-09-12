package com.rs.entity.player.infractions;
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

public final class ReportAbuse {

    private final String username;
    private final ReportAbuseRule rule;
    private final boolean muteFor48Hours;

    public ReportAbuse(String username, ReportAbuseRule rule, boolean muteFor48Hours) {
        this.username = username;
        this.rule = rule;
        this.muteFor48Hours = muteFor48Hours;
    }

    public String getUsername() {
        return username;
    }

    public ReportAbuseRule getRule() {
        return rule;
    }

    public boolean isMuteFor48Hours() {
        return muteFor48Hours;
    }
}
