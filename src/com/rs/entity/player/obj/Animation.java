package com.rs.entity.player.obj;

/**
 * A representation of a game animation.
 *
 * @author Pure_
 */
public final class Animation {

    private int id;
    private int delay;

    public Animation() {
        this(-1, -1);
    }

    public Animation(int id, int delay) {
        this.id = id;
        this.delay = delay;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public boolean isValid() {
        return id != -1 && delay != -1;
    }
}
