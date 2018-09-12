package com.rs.entity;

import com.rs.util.Tickable;

/**
 * An in-game entity.
 */
public abstract class Entity implements Tickable {

    private final MovementHandler movementHandler = new MovementHandler(this);
    private final EntityUpdateContext updateContext;
    private Position currentRegion = new Position(0, 0, 0);
    private int slot = -1;
    private int primaryDirection = -1;
    private int secondaryDirection = -1;
    private boolean needsPlacement = false;
    private boolean resetMovementQueue = false;

    public Entity(EntityUpdateContext updateContext) {
        this.updateContext = updateContext;
    }

    public abstract Position getPosition();

    /**
     * Resets the entity after updating.
     */
    public void reset() {
        setPrimaryDirection(-1);
        setSecondaryDirection(-1);
        setResetMovementQueue(false);
        setNeedsPlacement(false);
        updateContext.resetFlags();
    }

    public MovementHandler getMovementHandler() {
        return movementHandler;
    }

    public int getPrimaryDirection() {
        return primaryDirection;
    }

    /**
     * Sets the player's primary movement direction.
     */
    public void setPrimaryDirection(int primaryDirection) {
        this.primaryDirection = primaryDirection;
    }

    public int getSecondaryDirection() {
        return secondaryDirection;
    }

    /**
     * Sets the player's secondary movement direction.
     */
    public void setSecondaryDirection(int secondaryDirection) {
        this.secondaryDirection = secondaryDirection;
    }

    /**
     * Gets the current region.
     */
    public Position getCurrentRegion() {
        return currentRegion;
    }

    public void setCurrentRegion(Position currentRegion) {
        this.currentRegion = currentRegion;
    }

    public boolean isResetMovementQueue() {
        return resetMovementQueue;
    }

    public void setResetMovementQueue(boolean resetMovementQueue) {
        this.resetMovementQueue = resetMovementQueue;
    }

    public void setNeedsPlacement(boolean needsPlacement) {
        this.needsPlacement = needsPlacement;
    }

    /**
     * Gets whether or not the player needs to be placed.
     */
    public boolean needsPlacement() {
        return needsPlacement;
    }

    /**
     * Gets the player slot.
     */
    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public EntityUpdateContext getUpdateContext() {
        return updateContext;
    }
}
