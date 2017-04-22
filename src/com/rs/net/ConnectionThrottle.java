package com.rs.net;
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

import java.util.HashMap;
import java.util.Map;

/**
 * A failed connection attempt recorder, used to throttle failed login attempts.
 * Note: The cool down is not 100% accurate.
 */
public final class ConnectionThrottle {

    public static final int COOLDOWN = 60_000;
    private static final int MAX_ATTEMPTS = 5;
    private static final Map<String, Integer> map = new HashMap<>();

    public static void enter(String host) {
        if (map.containsKey(host)) {
            map.replace(host, map.get(host) + 1);
        } else {
            map.put(host, 1);
        }
    }

    public static boolean throttled(String host) {
        return map.getOrDefault(host, 0) >= MAX_ATTEMPTS;
    }

    public static void clear() {
        map.clear();
    }
}
