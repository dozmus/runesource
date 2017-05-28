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

import com.rs.entity.Entity;
import com.rs.entity.Position;

/**
 * A non-player-character.
 *
 * @author blakeman8192
 */
public final class Npc extends Entity {

    private final int npcId;
    private boolean visible = true;
    private Position position = new Position(0, 0);
    private boolean updateRequired;

    /**
     * Creates a new Npc.
     *
     * @param npcId the NPC ID
     */
    public Npc(int npcId) {
        this.npcId = npcId;
    }

    @Override
    public void tick() {
        getMovementHandler().tick();
    }

    public void reset() {
        super.reset();
        // TODO: Any NPC flag resetting
    }

    /**
     * Gets the NPC ID.
     */
    public int getNpcId() {
        return npcId;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    public boolean isUpdateRequired() {
        return updateRequired;
    }

    public void setUpdateRequired(boolean updateRequired) {
        this.updateRequired = updateRequired;
    }
}
