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
package com.rs.entity.player;

import com.rs.Server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The infractions associated with a given player, this does not support any IP-based infractions.
 */
public final class PlayerInfractions {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(Server.getInstance().getSettings().getDateFormat());
    private boolean banned;
    private boolean muted;
    private Date banExpirationDate;
    private Date muteExpirationDate;

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void setBanExpirationDate(Date banExpirationDate) {
        this.banExpirationDate = banExpirationDate;
    }

    public void setMuteExpirationDate(Date muteExpirationDate) {
        this.muteExpirationDate = muteExpirationDate;
    }

    public boolean isBanned() {
        return banned && (banExpirationDate == null || new Date().before(banExpirationDate));
    }

    public boolean isMuted() {
        return muted && (muteExpirationDate == null || new Date().before(muteExpirationDate));
    }

    public String banExpiration() {
        return banExpirationDate != null ? DATE_FORMAT.format(banExpirationDate) : "never";
    }

    public String muteExpiration() {
        return muteExpirationDate != null ? DATE_FORMAT.format(muteExpirationDate) : "never";
    }
}
