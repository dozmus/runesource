package com.rs.entity;
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

/**
 * The update flags for an {@link com.rs.entity.Entity}.
 */
public abstract class EntityUpdateContext {

    private boolean updateRequired = false;
    private boolean graphicsUpdateRequired = false;
    private boolean animationUpdateRequired = false;
    private boolean forcedChatUpdateRequired = false;
    private boolean faceCoordinatesUpdateRequired = false;
    private boolean primaryHitUpdateRequired = false;
    private boolean secondaryHitUpdateRequired = false;
    private boolean interactingNpcUpdateRequired = false;

    public abstract int mask();

    public void setUpdateRequired() {
        updateRequired = true;
    }

    public void setGraphicsUpdateRequired() {
        updateRequired = true;
        graphicsUpdateRequired = true;
    }

    public void setAnimationUpdateRequired() {
        updateRequired = true;
        animationUpdateRequired = true;
    }

    public void setForcedChatUpdateRequired() {
        updateRequired = true;
        forcedChatUpdateRequired = true;
    }

    public void setInteractingNpcUpdateRequired() {
        updateRequired = true;
        interactingNpcUpdateRequired = true;
    }

    public void setFaceCoordinatesUpdateRequired() {
        updateRequired = true;
        faceCoordinatesUpdateRequired = true;
    }

    public void setPrimaryHitUpdateRequired() {
        updateRequired = true;
        primaryHitUpdateRequired = true;
    }

    public void setSecondaryHitUpdateRequired() {
        updateRequired = true;
        secondaryHitUpdateRequired = true;
    }

    public boolean isUpdateRequired() {
        return updateRequired;
    }

    public boolean isGraphicsUpdateRequired() {
        return graphicsUpdateRequired;
    }

    public boolean isAnimationUpdateRequired() {
        return animationUpdateRequired;
    }

    public boolean isForcedChatUpdateRequired() {
        return forcedChatUpdateRequired;
    }

    public boolean isInteractingNpcUpdateRequired() {
        return interactingNpcUpdateRequired;
    }

    public boolean isFaceCoordinatesUpdateRequired() {
        return faceCoordinatesUpdateRequired;
    }

    public boolean isPrimaryHitUpdateRequired() {
        return primaryHitUpdateRequired;
    }

    public boolean isSecondaryHitUpdateRequired() {
        return secondaryHitUpdateRequired;
    }

    /**
     * Resets all update flags.
     */
    public void resetFlags() {
        updateRequired = false;
        graphicsUpdateRequired = false;
        animationUpdateRequired = false;
        forcedChatUpdateRequired = false;
        interactingNpcUpdateRequired = false;
        faceCoordinatesUpdateRequired = false;
        primaryHitUpdateRequired = false;
        secondaryHitUpdateRequired = false;
    }
}
