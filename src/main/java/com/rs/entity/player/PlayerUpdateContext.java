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
import com.rs.net.StreamBuffer;

import java.nio.ByteBuffer;

/**
 * The update flags and buffer caches for a {@link Player}.
 */
public final class PlayerUpdateContext extends EntityUpdateContext {

    private final StreamBuffer.BufferCache regularBufferCache = new StreamBuffer.BufferCache();
    private final StreamBuffer.BufferCache forcedAppearanceBufferCache = new StreamBuffer.BufferCache();
    private final StreamBuffer.BufferCache noChatBufferCache = new StreamBuffer.BufferCache();
    private final StreamBuffer.BufferCache forcedAppearanceNoChatBufferCache = new StreamBuffer.BufferCache();
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

    public void setUpdateRequired() {
        super.setUpdateRequired();
        setAllBuffersOutdated();
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
        setAllBuffersOutdated();
    }

    public void setBuffer(boolean forceAppearance, boolean noPublicChat, ByteBuffer buffer) {
        if (!forceAppearance && !noPublicChat) {
            regularBufferCache.setBuffer(buffer);
        } else if (forceAppearance && !noPublicChat) {
            forcedAppearanceBufferCache.setBuffer(buffer);
        } else if (!forceAppearance && noPublicChat) {
            noChatBufferCache.setBuffer(buffer);
        } else {
            forcedAppearanceNoChatBufferCache.setBuffer(buffer);
        }
    }

    public ByteBuffer getBuffer(boolean forceAppearance, boolean noPublicChat) {
        if (!forceAppearance && !noPublicChat && !regularBufferCache.isOutdated()) {
            return regularBufferCache.getBuffer();
        } else if (forceAppearance && !noPublicChat && !forcedAppearanceBufferCache.isOutdated()) {
            return forcedAppearanceBufferCache.getBuffer();
        } else if (!forceAppearance && noPublicChat && !noChatBufferCache.isOutdated()) {
            return noChatBufferCache.getBuffer();
        } else if (forceAppearance && noPublicChat && !forcedAppearanceNoChatBufferCache.isOutdated()) {
            return forcedAppearanceNoChatBufferCache.getBuffer();
        }
        return null;
    }

    private void setAllBuffersOutdated() {
        regularBufferCache.setOutdated();
        forcedAppearanceBufferCache.setOutdated();
        noChatBufferCache.setOutdated();
        forcedAppearanceNoChatBufferCache.setOutdated();
    }
}
