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

import java.util.concurrent.ConcurrentHashMap;

/**
 * A static gateway type class that is used to limit the maximum amount of
 * connections per host.
 * 
 * @author blakeman8192
 */
public class HostGateway {

	/** The maximum amount of connections per host. */
	public static final int MAX_CONNECTIONS_PER_HOST = 5;

	/** Used to keep track of hosts and their amount of connections. */
	private static ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();

	/**
	 * Checks the host into the gateway.
	 * 
	 * @param host
	 *            the host
	 * @return true if the host can connect, false if it has reached the maximum
	 *         amount of connections
	 */
	public static boolean enter(String host) {
		Integer amount = map.putIfAbsent(host, 1);

		// If the host was not in the map, they're clear to go.
		if (amount == null) {
			return true;
		}

		// If they've reached the connection limit, return false.
		if (amount == MAX_CONNECTIONS_PER_HOST) {
			return false;
		}

		// Otherwise, replace the key with the next value if it was present.
		map.replace(host, amount + 1);
		return true;
	}

	/**
	 * Unchecks the host from the gateway.
	 * 
	 * @param host
	 *            the host
	 */
	public static void exit(String host) {
		Integer amount = map.get(host);

		// Remove the host from the map if it's at 1 connection.
		if (amount == 1) {
			map.remove(host);
			return;
		}

		// Otherwise decrement the amount of connections stored.
		if (amount != null) {
			map.replace(host, amount - 1);
		}
	}

}
