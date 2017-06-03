package com.rs.entity.npc;
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
 * The update flags for a {@link Npc}.
 */
public final class NpcUpdateContext extends EntityUpdateContext {

    // TODO put buffers here to cache
    private boolean npcDefinitionUpdateRequired = false;

    public int mask() {
        int mask = 0x0;

        if (isAnimationUpdateRequired()) {
            mask |= 0x10;
        }

        if (isPrimaryHitUpdateRequired()) {
            mask |= 0x40;
        }

        if (isGraphicsUpdateRequired()) {
            mask |= 0x80;
        }

        if (isInteractingNpcUpdateRequired()) {
            mask |= 0x20;
        }

        if (isForcedChatUpdateRequired()) {
            mask |= 0x1;
        }

        if (isSecondaryHitUpdateRequired()) {
            mask |= 0x8;
        }

        if (isNpcDefinitionUpdateRequired()) {
            mask |= 0x2;
        }

        if (isFaceCoordinatesUpdateRequired()) {
            mask |= 0x4;
        }
        return mask;
    }

    public void setUpdateRequired() {
        super.setUpdateRequired();
    }

    public void setNpcDefintionUpdateRequired() {
        setUpdateRequired();
        npcDefinitionUpdateRequired = true;
    }

    public boolean isNpcDefinitionUpdateRequired() {
        return npcDefinitionUpdateRequired;
    }

    public void resetFlags() {
        super.resetFlags();
        npcDefinitionUpdateRequired = false;
    }
}
