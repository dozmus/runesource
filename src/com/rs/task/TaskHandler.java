package com.rs.task;

import com.rs.entity.player.Player;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A simple handler to manage tasks.
 * @author Pure_
 */
public final class TaskHandler {

    /**
     * A collection of tasks.
     */
    private static ArrayList<Task> tasks = new ArrayList<Task>();

    /**
     * Performs a processing task on all active tasks.
     */
    public static void tick() {
        for (Iterator<Task> itr = tasks.iterator(); itr.hasNext();) {
            Task task = itr.next();

            // Removing completed tasks
            if (!task.isRunning()) {
                itr.remove();
                continue;
            }

            // Processing task
            task.tick();
        }
    }

    /**
     * Deactivates all tasks which belong to the given player.
     * @param player
     */
    public static void removeTasks(Player player) {
        for (Task task : tasks) {
            if (task.getPlayer().equals(player)) {
                task.setRunning(false);
            }
        }
    }

    /**
     * Adds a task to the execution list.
     * @param task
     */
    public static void submit(Task task) {
        tasks.add(task);
    }
}
