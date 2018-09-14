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

import com.rs.entity.npc.Npc;
import com.rs.entity.npc.NpcUpdating;
import com.rs.entity.player.Player;
import com.rs.entity.player.PlayerUpdating;
import com.rs.plugin.PluginHandler;
import com.rs.task.TaskHandler;
import com.rs.util.Tickable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

/**
 * Handles all logged in players.
 *
 * @author blakeman8192
 */
public final class WorldHandler implements Tickable {

    /**
     * Singleton instance.
     */
    private static final WorldHandler instance = new WorldHandler();


    /**
     * All registered players.
     */
    private final Player[] players = new Player[2048];
    /**
     * All registered NPCs.
     */
    private final Npc[] npcs = new Npc[8192];

    /**
     * A thread pool that allows tasks to be executed in parallel.
     */
    private final ExecutorService threadPool = Executors.newFixedThreadPool(Server.CPU_CORES);

    /**
     * A synchronization barrier that allows a group of threads to wait for each other to
     * arrive at a certain point. This is needed so the world can be concurrently updated in phases.
     */
    private final Phaser barrier = new Phaser(1);

    private int playerAmount = 0;
    private int npcAmount = 0;

    /**
     * @return Singleton instance.
     */

    public static WorldHandler getInstance() {
        return instance;
    }

    /**
     * Performs the processing of all world functions.
     */
    public void tick() throws Exception {
        // XXX: Maybe we could implement loop fusion to speed this up.

        // Perform any logic processing for players.
        for (Player player : players) {
            if (player == null) {
                continue;
            }

            try {
                player.tick();
            } catch (Exception ex) {
                ex.printStackTrace();
                player.disconnect();
            }
        }

        // Perform any logic processing for NPCs.
        for (Npc npc : npcs) {
            if (npc == null) {
                continue;
            }

            try {
                npc.tick();
            } catch (Exception ex) {
                ex.printStackTrace();
                unregister(npc);
            }
        }

        // Process all plugins.
        PluginHandler.dispatchTick();

        // Process all tasks
        TaskHandler.tick();

        // Update all players.
        barrier.bulkRegister(playerAmount);
        for (Player player : players) {
            if (player == null) {
                continue;
            }
            threadPool.execute(() -> {
                synchronized (player) {
                    try {
                        PlayerUpdating.update(player);
                        NpcUpdating.update(player);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        barrier.arriveAndDeregister();
                    }
                }
            });

        }
        barrier.arriveAndAwaitAdvance();


        // Reset all players after tick.
        for (Player player : players) {
            if (player == null) {
                continue;
            }

            try {
                player.reset();
            } catch (Exception ex) {
                ex.printStackTrace();
                player.disconnect();
            }
        }

        // Reset all NPCs after tick.
        for (Npc npc : npcs) {
            if (npc == null) {
                continue;
            }

            try {
                npc.reset();
            } catch (Exception ex) {
                ex.printStackTrace();
                unregister(npc);
            }
        }
    }

    /**
     * Registers a player for processing.
     */
    public void register(Player player) {
        for (int i = 1; i < players.length; i++) {
            if (players[i] == null) {
                players[i] = player;
                player.setSlot(i);
                playerAmount++;
                return;
            }
        }
        throw new IllegalStateException("Server is full!");
    }

    /**
     * Registers an NPC for processing.
     */
    public void register(Npc npc) {
        for (int i = 1; i < npcs.length; i++) {
            if (npcs[i] == null) {
                npcs[i] = npc;
                npc.setSlot(i);
                npcAmount++;
                return;
            }
        }
        throw new IllegalStateException("Server is full!");
    }

    /**
     * Unregisters a player from processing.
     */
    public void unregister(Player player) {
        if (player.getSlot() == -1) {
            return;
        }
        players[player.getSlot()] = null;
        playerAmount--;
    }

    /**
     * Unregisters an NPC from processing.
     */
    public void unregister(Npc npc) {
        if (npc.getSlot() == -1) {
            return;
        }
        npcs[npc.getSlot()] = null;
        npcAmount--;
    }

    public boolean isPlayerOnline(String username) {
        if (playerAmount == 0)
            return false;

        for (Player player : players) {
            if (player == null)
                continue;

            if (player.getAttributes().getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public Player getPlayer(String username) {
        if (playerAmount == 0)
            throw new IndexOutOfBoundsException();

        for (Player player : players) {
            if (player == null)
                continue;

            if (player.getAttributes().getUsername().equals(username)) {
                return player;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public static void trySendMessage(String username, String message) {
        try {
            Player p = WorldHandler.getInstance().getPlayer(username);
            p.sendMessage(message);
        } catch (IndexOutOfBoundsException e) {
        }
    }

    /**
     * @return the amount of online players
     */
    public int playerAmount() {
        return playerAmount;
    }

    /**
     * @return the amount of online NPCs
     */
    public int npcAmount() {
        return npcAmount;
    }

    /**
     * Gets all registered players.
     */
    public Player[] getPlayers() {
        return players;
    }

    /**
     * Gets all registered NPCs.
     */
    public Npc[] getNpcs() {
        return npcs;
    }

}
