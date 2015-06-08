package com.rs.task;

import com.rs.entity.player.Player;

/**
 * A simple task base class.
 *
 * @author Pure_
 */
public abstract class Task {

    private boolean running;
    private int currentDelay;
    private int delay;
    private boolean runOnce;
    private Player player;
    private Object[] args;
    private int ticks;

    /**
     * Creates a new task instance which will run once with no arguments.
     *
     * @param delay
     * @param player
     */
    public Task(int delay, Player player) {
        this(delay, true, player);
    }

    /**
     * Creates a new task instance.
     *
     * @param delay   the delay (in server ticks) until the task is to be executed
     * @param runOnce if the task should only be executed once
     * @param player  the player this task is bound to
     * @param args    the args to provide to the instance
     */
    public Task(int delay, boolean runOnce, Player player, Object... args) {
        this.delay = delay;
        this.runOnce = runOnce;
        this.player = player;
        this.args = args;
        this.running = true;
        currentDelay = delay;
    }

    /**
     * Contains the task's executable code, this is executed once the delay is depleted.
     */
    protected abstract void process();

    /**
     * Performs a logic tick, this checks if the task can be executed yet.
     */
    public void tick() {
        if (running && currentDelay-- <= 0) {
            process();
            ticks++;
            currentDelay = delay;

            if (runOnce) {
                running = false;
            }
        }
    }

    /**
     * Whether or not the task should be removed.
     *
     * @return
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Sets if the task should be removed next tick.
     *
     * @param
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Gets the array of arguments passed to the task upon creation.
     *
     * @return
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * The player this task is bound to.
     *
     * @return
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * The amount of times this task has been executed.
     *
     * @return
     */
    public int getTicks() {
        return ticks;
    }
}
