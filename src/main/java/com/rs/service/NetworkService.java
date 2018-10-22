package com.rs.service;
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

import com.rs.Server;
import com.rs.entity.player.Client;
import com.rs.entity.player.Player;
import com.rs.net.ConnectionThrottle;
import com.rs.net.HostGateway;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A service which accepts connections, and handles data reading.
 */
public class NetworkService implements Service {

    private ServerSocketChannel serverChannel;
    private Map<SelectionKey, Client> clientMap;
    private Selector selector;

    public void init() throws Exception {
        // Initialize the networking objects.
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();

        // ... and configure them!
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(Server.getInstance().getAddress());
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Finally, initialize whatever else we need.
        clientMap = new HashMap<>();
    }

    public void tick() {
        // Remove disconnected clients
        clientMap.entrySet().removeIf(e -> !e.getKey().isValid());

        // Remove clients which timed out during login
        List<SelectionKey> timedOutKeys = clientMap.entrySet().stream()
                .filter(e -> e.getValue().getTimeoutStopwatch().elapsed() > 5000)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        timedOutKeys.forEach(e -> {
            clientMap.get(e).disconnect();
            clientMap.remove(e);
            e.cancel();
        });

        // Handle all network events
        try {
            selector.selectNow();

            for (SelectionKey selectionKey : selector.selectedKeys()) {
                if (selectionKey.isAcceptable()) { // Accept new connections.
                    accept(10);
                }

                if (selectionKey.isReadable()) { // Client handles the packet.
                    clientMap.get(selectionKey).handleIncomingData();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Check if connection throttling needs a clear
        if (System.currentTimeMillis() % ConnectionThrottle.COOLDOWN == 0) {
            ConnectionThrottle.clear();
        }
    }

    public void cleanup() {
        Map<SelectionKey, Client> client = new HashMap<>(clientMap);
        client.forEach((k, v) -> {
            if (v.getConnectionStage() != Client.ConnectionStage.LOGGED_OUT) {
                v.disconnect();
            }
        });
    }

    /**
     * Accepts up to n incoming connections.
     */
    private void accept(int n) throws IOException {
        SocketChannel socket;

        /*
         * Here we use a for loop so that we can accept multiple clients per
         * tick for lower latency. We limit the amount of clients that we
         * accept per tick to better combat potential denial of service type
         * attacks.
         */
        for (int i = 0; i < n; i++) {
            socket = serverChannel.accept();

            if (socket == null) {
                // No more connections to accept (as this one was invalid).
                break;
            }

            // Register the connection
            HostGateway.enter(socket.socket().getInetAddress().getHostAddress());

            // Set up the new connection.
            socket.configureBlocking(false);
            SelectionKey key = socket.register(selector, SelectionKey.OP_READ);
            Client client = new Player(key);
            System.out.println("Accepted " + client + ".");
            clientMap.put(key, client);
        }
    }
}
