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
import com.rs.entity.action.*;

/**
 * A Non-Player Character.
 *
 * @author blakeman8192
 */
public final class Npc extends Entity {

    private final int id;
    private boolean visible = true;
    private Position position = new Position(0, 0);
    // Various npc update data.
    private String forceChatText;
    private Animation animation;
    private Graphics graphics;
    private Hit primaryHit;
    private Hit secondaryHit;
    private Position facingPosition;
    private Npc interactingNpc;
    private int npcDefinitionId;

    /**
     * Creates a new Npc.
     *
     * @param id the NPC ID
     */
    public Npc(int id) {
        super(new NpcUpdateContext());
        this.id = id;
    }

    @Override
    public void tick() {
        getMovementHandler().tick();
    }

    public void reset() {
        super.reset();
    }

    /**
     * Gets the NPC ID.
     */
    public int getId() {
        return id;
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

    public NpcUpdateContext getUpdateContext() {
        return (NpcUpdateContext)super.getUpdateContext();
    }

    public String getForceChatText() {
        return forceChatText;
    }

    public void setForceChatText(String forceChatText) {
        this.forceChatText = forceChatText;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public Graphics getGraphics() {
        return graphics;
    }

    public void setGraphics(Graphics graphics) {
        this.graphics = graphics;
    }

    public Hit getPrimaryHit() {
        return primaryHit;
    }

    public void setPrimaryHit(Hit primaryHit) {
        this.primaryHit = primaryHit;
    }

    public Hit getSecondaryHit() {
        return secondaryHit;
    }

    public void setSecondaryHit(Hit secondaryHit) {
        this.secondaryHit = secondaryHit;
    }

    public Position getFacingPosition() {
        return facingPosition;
    }

    public void setFacingPosition(Position facingPosition) {
        this.facingPosition = facingPosition;
    }

    public Npc getInteractingNpc() {
        return interactingNpc;
    }

    public void setInteractingNpc(Npc interactingNpc) {
        this.interactingNpc = interactingNpc;
    }

    public int getNpcDefinitionId() {
        return npcDefinitionId;
    }

    public void setNpcDefinitionId(int npcDefinitionId) {
        this.npcDefinitionId = npcDefinitionId;
    }

    public int getCurrentHealth() {
        return 0;
    }

    public int getMaximumHealth() {
        return 0;
    }
}
