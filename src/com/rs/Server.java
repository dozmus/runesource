package com.rs;
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

import com.rs.entity.player.Client;
import com.rs.entity.player.Player;
import com.rs.io.JsonFileHandler;
import com.rs.io.PlayerFileHandler;
import com.rs.net.HostGateway;
import com.rs.plugin.PluginHandler;
import com.rs.util.Misc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * The main core of RuneSource.
 *
 * @author blakeman8192
 */
public class Server implements Runnable {

    private static Server instance;
    private final String host;
    private final int port;
    private final int cycleRate;

    private Settings settings;
    private PlayerFileHandler playerFileHandler;
    private Selector selector;
    private InetSocketAddress address;
    private ServerSocketChannel serverChannel;
    private Misc.Stopwatch cycleTimer;
    private Map<SelectionKey, Client> clientMap;

    /**
     * Creates a new Server.
     *
     * @param host      the host
     * @param port      the port
     * @param cycleRate the tick rate
     */
    private Server(String host, int port, int cycleRate) {
        this.host = host;
        this.port = port;
        this.cycleRate = cycleRate;
    }

    /**
     * The main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: Server <host> <port> <cycleRate>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int cycleRate = Integer.parseInt(args[2]);

        setInstance(new Server(host, port, cycleRate));
        new Thread(getInstance()).start();
    }

    /**
     * Gets the server instance object.
     *
     * @return the instance
     */
    public static Server getInstance() {
        return instance;
    }

    /**
     * Sets the server instance object.
     *
     * @param instance the instance
     */
    public static void setInstance(Server instance) {
        if (Server.instance != null) {
            throw new IllegalStateException("Singleton already set!");
        }
        Server.instance = instance;
    }

    @Override
    public void run() {
        try {
            System.setOut(new Misc.TimestampLogger(System.out, "./data/logs/out.log"));
            System.setErr(new Misc.TimestampLogger(System.err, "./data/logs/err.log"));

            address = new InetSocketAddress(host, port);
            System.out.println("Starting RuneSource on " + address + "...");

            // Loading configuration
            Misc.Stopwatch timer = new Misc.Stopwatch();
            settings = Settings.load("./data/settings.json");
            Misc.sortEquipmentSlotDefinitions();
            Misc.loadStackableItems("./data/stackable.dat");
            playerFileHandler = new JsonFileHandler();
            System.out.println("Loaded all configuration in " + timer.elapsed() + "ms");

            // Loading plugins
            timer.reset();
            PluginHandler.loadPlugins("./data/plugins.ini");
            System.out.println("Loaded all plugins in " + timer.elapsed() + "ms");

            // Starting the server
            init();
            System.out.println("Started as " + settings.getServerName());

            while (true) {
                cycle();
                sleep();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Initialises the server.
     *
     * @throws IOException
     */
    private void init() throws IOException {
        // Initialize the networking objects.
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();

        // ... and configure them!
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(address);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Finally, initialize whatever else we need.
        cycleTimer = new Misc.Stopwatch();
        clientMap = new HashMap<SelectionKey, Client>();
    }

    /**
     * Accepts any incoming connections.
     *
     * @throws IOException
     */
    private void accept() throws IOException {
        SocketChannel socket;

		/*
         * Here we use a for loop so that we can accept multiple clients per
		 * tick for lower latency. We limit the amount of clients that we
		 * accept per tick to better combat potential denial of service type
		 * attacks.
		 */
        for (int i = 0; i < 10; i++) {
            socket = serverChannel.accept();

            if (socket == null) {
                // No more connections to accept (as this one was invalid).
                break;
            }

            // Make sure we can allow this connection.
            if (!HostGateway.enter(socket.socket().getInetAddress().getHostAddress())) {
                socket.close();
                continue;
            }

            // Set up the new connection.
            socket.configureBlocking(false);
            SelectionKey key = socket.register(selector, SelectionKey.OP_READ);
            Client client = new Player(key);
            System.out.println("Accepted " + client + ".");
            getClientMap().put(key, client);
        }
    }

    /**
     * Performs a server tick.
     */
    private void cycle() {
        // First, handle all network events.
        try {
            selector.selectNow();

            for (SelectionKey selectionKey : selector.selectedKeys()) {
                if (selectionKey.isAcceptable()) {
                    accept(); // Accept a new connection.
                }

                if (selectionKey.isReadable()) {
                    // Tell the client to handle the packet.
                    getClientMap().get(selectionKey).handleIncomingData();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Next, perform game processing.
        try {
            WorldHandler.process();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sleeps for the tick delay.
     *
     * @throws InterruptedException
     */
    private void sleep() throws InterruptedException {
        long sleepTime = cycleRate - cycleTimer.elapsed();
        if (sleepTime > 0) {
            Thread.sleep(sleepTime);
        } else {
            // The server has reached maximum load, players may now lag.
            System.out.println("[WARNING]: Server load: " + (100 + (Math.abs(sleepTime) / (cycleRate / 100))) + "%!");
        }
        cycleTimer.reset();
    }

    /**
     * Gets the client map.
     *
     * @return the client map
     */
    public Map<SelectionKey, Client> getClientMap() {
        return clientMap;
    }

    public PlayerFileHandler getPlayerFileHandler() {
        return playerFileHandler;
    }

    public Settings getSettings() {
        return settings;
    }
}
