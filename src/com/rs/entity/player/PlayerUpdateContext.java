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

import com.rs.entity.EntityUpdateContext;

/**
 * The update flags for a {@link Player}.
 */
public final class PlayerUpdateContext extends EntityUpdateContext {

    // TODO put buffers here to cache
    private boolean asyncMovementUpdateRequired = false;
    private boolean publicChatUpdateRequired = false;
    private boolean appearanceUpdateRequired = false;

    public int mask() {
        return mask(false, false);
    }

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

    public void setAsyncMovementUpdateRequired() {
        setUpdateRequired();
        asyncMovementUpdateRequired = true;
    }

    public void setPublicChatUpdateRequired() {
        setUpdateRequired();
        publicChatUpdateRequired = true;
    }

    public void setAppearanceUpdateRequired() {
        setUpdateRequired();
        appearanceUpdateRequired = true;
    }

    public boolean isAsyncMovementUpdateRequired() {
        return asyncMovementUpdateRequired;
    }

    public boolean isPublicChatUpdateRequired() {
        return publicChatUpdateRequired;
    }

    public boolean isAppearanceUpdateRequired() {
        return appearanceUpdateRequired;
    }

    /**
     * Resets all update flags.
     */
    public void resetFlags() {
        super.resetFlags();
        asyncMovementUpdateRequired = false;
        publicChatUpdateRequired = false;
        appearanceUpdateRequired = false;
    }
}
