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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.rs.io.PlayerFileHandler;
import com.rs.plugin.PluginHandler;
import com.rs.service.Service;
import com.rs.util.AbstractCredentialValidator;
import com.rs.util.EquipmentHelper;
import com.rs.util.Misc;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Set;

/**
 * The main core of RuneSource.
 *
 * @author blakeman8192
 */
public final class Server implements Runnable, Service {


    /**
     * The amount of CPU processors available.
     */
    public static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static Server instance;
    private final String host;
    private final int port;
    private final int tickRate;
    private final Set<Service> services;
    private final PlayerFileHandler playerFileHandler;
    private final AbstractCredentialValidator credentialValidator;

    private Settings settings;
    private InetSocketAddress address;
    private Misc.Stopwatch cycleTimer;

    /**
     * Creates a new Server.
     *
     * @param host     the host
     * @param port     the port
     * @param tickRate the tick rate
     */
    @Inject
    private Server(@Named("host") String host, @Named("port") int port, @Named("tickRate") int tickRate,
                   Set<Service> services, PlayerFileHandler playerFileHandler,
                   AbstractCredentialValidator credentialValidator) {
        this.host = host;
        this.port = port;
        this.tickRate = tickRate;
        this.services = services;
        this.playerFileHandler = playerFileHandler;
        this.credentialValidator = credentialValidator;
    }

    /**
     * The main method.
     */
    public static void main(String[] args) {
        // Parse command line arguments
        String host = "127.0.0.1";
        int port = 43594;
        int tickRate = 600;

        if (args.length == 3) {
            host = args[0];
            port = Integer.parseInt(args[1]);
            tickRate = Integer.parseInt(args[2]);
        } else if (args.length != 0) {
            System.err.println("Usage: Server <host> <port> <cycleRate>");
            return;
        }

        // Validate command line arguments
        if (port < 0 || port > 65535) {
            System.err.println("Invalid port number, must be between 0 and 65535.");
            return;
        }

        if (tickRate < 0) {
            System.err.println("Invalid cycle rate, must be a positive integer.");
            return;
        }

        // Create server
        Injector injector = Guice.createInjector(new ServerModule(host, port, tickRate));
        Server server = injector.getInstance(Server.class);

        // Start server
        setInstance(server);
        new Thread(getInstance()).start();
    }

    @Override
    public void run() {
        try {
            address = new InetSocketAddress(host, port);
            System.out.println("Starting RuneSource on " + address + "...");

            // Load settings
            settings = Settings.load("./data/settings.json");

            // Set up out, err redirection
            new File("./data/logs").mkdir();
            System.setOut(new Misc.TimestampLogger(System.out, "./data/logs/out.log"));
            System.setErr(new Misc.TimestampLogger(System.err, "./data/logs/err.log"));

            // Loading configuration
            Misc.Stopwatch timer = new Misc.Stopwatch();
            EquipmentHelper.loadWeaponDefinitions("./data/weapons.json");
            EquipmentHelper.sortEquipmentSlotDefinitions();
            EquipmentHelper.loadStackableItems("./data/stackable.dat");
            System.out.println("Loaded all configuration in " + timer.elapsed() + "ms");

            // Loading plugins
            timer.reset();
            PluginHandler.load();
            System.out.println("Loaded all plugins in " + timer.elapsed() + "ms");

            // Starting the server
            init();
            System.out.println("Started as " + settings.getServerName());

            while (true) {
                tick();
                sleep();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Initialises the server.
     */
    public void init() throws Exception {
        // Init
        for (Service service : services) {
            service.init();
        }

        // Set shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));

        cycleTimer = new Misc.Stopwatch();
    }

    /**
     * Performs a server tick.
     */
    public void tick() {
        services.forEach(Service::tick);
    }

    /**
     * Cleans up the server.
     */
    public void cleanup() {
        services.forEach(Service::cleanup);
    }

    /**
     * Sleeps for the remainder of the tick time slice.
     */
    private void sleep() throws InterruptedException {
        long sleepTime = tickRate - cycleTimer.elapsed();

        if (sleepTime > 0) {
            Thread.sleep(sleepTime);
        } else {
            // The server has reached maximum load, players may now lag.
            System.out.println("[WARNING]: Server load: " + (100 + (Math.abs(sleepTime) / (tickRate / 100))) + "%!");
        }
        cycleTimer.reset();
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public PlayerFileHandler getPlayerFileHandler() {
        return playerFileHandler;
    }

    public AbstractCredentialValidator getCredentialValidator() {
        return credentialValidator;
    }

    public Settings getSettings() {
        return settings;
    }

    /**
     * Gets the server instance.
     */
    public static Server getInstance() {
        return instance;
    }

    /**
     * Sets the server instance.
     */
    public static void setInstance(Server instance) {
        if (Server.instance != null) {
            throw new IllegalStateException("Singleton already set!");
        }
        Server.instance = instance;
    }
}
