package com.rs.entity.player;
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
 * The update flags for a {@link Player}.
 */
public final class PlayerUpdateContext {

    // TODO put buffers here to cache
    private boolean updateRequired = false;
    private boolean asyncMovementUpdateRequired = false;
    private boolean graphicsUpdateRequired = false;
    private boolean animationUpdateRequired = false;
    private boolean forcedChatUpdateRequired = false;
    private boolean publicChatUpdateRequired = false;
    private boolean interactingNpcUpdateRequired = false;
    private boolean appearanceUpdateRequired = false;
    private boolean faceCoordinatesUpdateRequired = false;
    private boolean primaryHitUpdateRequired = false;
    private boolean secondaryHitUpdateRequired = false;

    public int mask(boolean forceAppearance, boolean noPublicChat) {
        int mask = 0x0;

        if (isAsyncMovementUpdateRequired()) {
            mask |= 0x400;
        }

        if (isGraphicsUpdateRequired()) {
            mask |= 0x100;
        }

        if (isAnimationUpdateRequired()) {
            mask |= 0x8;
        }

        if (isForcedChatUpdateRequired()) {
            mask |= 0x4;
        }

        if (isPublicChatUpdateRequired() && !noPublicChat) {
            mask |= 0x80;
        }

        if (isInteractingNpcUpdateRequired()) {
            mask |= 0x1;
        }

        if (isAppearanceUpdateRequired() || forceAppearance) {
            mask |= 0x10;
        }

        if (isFaceCoordinatesUpdateRequired()) {
            mask |= 0x2;
        }

        if (isPrimaryHitUpdateRequired()) {
            mask |= 0x20;
        }

        if (isSecondaryHitUpdateRequired()) {
            mask |= 0x200;
        }
        return mask;
    }

    public void setUpdateRequired() {
        updateRequired = true;
    }

    public void setAsyncMovementUpdateRequired() {
        updateRequired = true;
        asyncMovementUpdateRequired = true;
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

    public void setPublicChatUpdateRequired() {
        updateRequired = true;
        publicChatUpdateRequired = true;
    }

    public void setInteractingNpcUpdateRequired() {
        updateRequired = true;
        interactingNpcUpdateRequired = true;
    }

    public void setAppearanceUpdateRequired() {
        updateRequired = true;
        appearanceUpdateRequired = true;
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

    public boolean isAsyncMovementUpdateRequired() {
        return asyncMovementUpdateRequired;
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

    public boolean isPublicChatUpdateRequired() {
        return publicChatUpdateRequired;
    }

    public boolean isInteractingNpcUpdateRequired() {
        return interactingNpcUpdateRequired;
    }

    public boolean isAppearanceUpdateRequired() {
        return appearanceUpdateRequired;
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
        asyncMovementUpdateRequired = false;
        graphicsUpdateRequired = false;
        animationUpdateRequired = false;
        forcedChatUpdateRequired = false;
        publicChatUpdateRequired = false;
        interactingNpcUpdateRequired = false;
        appearanceUpdateRequired = false;
        faceCoordinatesUpdateRequired = false;
        primaryHitUpdateRequired = false;
        secondaryHitUpdateRequired = false;
    }
}
