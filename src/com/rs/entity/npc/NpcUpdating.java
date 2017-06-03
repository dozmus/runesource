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

import com.rs.WorldHandler;
import com.rs.entity.Position;
import com.rs.entity.player.Player;
import com.rs.net.StreamBuffer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides static utility methods for updating NPCs.
 *
 * @author blakeman8192
 */
public final class NpcUpdating {

    /**
     * Regional player limit.
     */
    private static final int REGION_NPCS_LIMIT = 255;

    /**
     * Updates all NPCs for the argued Player.
     *
     * @param player the player
     */
    public static void update(Player player) {
        // TODO prioritise updating NPCs based on distance to you
        // Find other NPCs in local region
        List<Npc> regionalNpcs = new ArrayList<>();

        for (Npc npc : WorldHandler.getInstance().getNpcs()) {
            if (player.getNpcs().size() + regionalNpcs.size() >= REGION_NPCS_LIMIT) {
                break; // Local player limit has been reached.
            }

            if (npc == null || player.getNpcs().contains(npc) || !npc.isVisible()
                    ||!npc.getPosition().isViewableFrom(player.getPosition())) {
                continue;
            }
            regionalNpcs.add(npc);
        }

        // Calculate state block size
        int localPlayerCount = player.getNpcs().size();
        int stateBlockSize = 0;

        for (Npc npc : player.getNpcs()) {
            stateBlockSize += stateBlockSize(player, npc, false);
        }

        for (Npc npc : regionalNpcs) {
            stateBlockSize += stateBlockSize(player, npc, true);
        }
        int totalBlockSize = 6 + stateBlockSize + 2*localPlayerCount + 5*regionalNpcs.size();

        // Create write buffers
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(totalBlockSize);
        StreamBuffer.WriteBuffer stateBlock = StreamBuffer.createWriteBuffer(stateBlockSize);

        // Initialize the update packet.
        out.writeVariableShortHeader(player.getEncryptor(), 65);
        out.setAccessType(StreamBuffer.AccessType.BIT_ACCESS);

        // Update the NPCs in the local list.
        out.writeBits(8, localPlayerCount);

        for (Iterator<Npc> i = player.getNpcs().iterator(); i.hasNext(); ) {
            Npc npc = i.next();

            if (npc.getPosition().isViewableFrom(player.getPosition()) && npc.isVisible()) {
                NpcUpdating.updateNpcMovement(out, npc);

                if (npc.getUpdateContext().isUpdateRequired()) {
                    NpcUpdating.updateState(stateBlock, npc);
                }
            } else {
                // Remove the NPC from the local list.
                out.writeBit(true);
                out.writeBits(2, 3);
                i.remove();
            }
        }

        // Update the local NPC list itself.
        for (Npc npc : regionalNpcs) {
            player.getNpcs().add(npc);
            addNpc(out, player, npc);
            NpcUpdating.updateState(stateBlock, npc);
        }

        // Append the update block to the packet if need be.
        if (stateBlock.getBuffer().position() > 0) {
            out.writeBits(14, 16383);
            out.setAccessType(StreamBuffer.AccessType.BYTE_ACCESS);
            out.writeBytes(stateBlock.getBuffer());
        } else {
            out.setAccessType(StreamBuffer.AccessType.BYTE_ACCESS);
        }

        // Ship the packet out to the client.
        out.finishVariableShortHeader();
        player.send(out.getBuffer());
    }

    /**
     * The size in bytes of the state block for the given npc.
     */
    private static int stateBlockSize(Player player, Npc npc, boolean forceAccept) {
        NpcUpdateContext ctx = npc.getUpdateContext();

        if (!forceAccept)
            if (!ctx.isUpdateRequired() || !npc.getPosition().isViewableFrom(player.getPosition()) || npc.isVisible())
                return 0;
        return 2 + (ctx.isGraphicsUpdateRequired() ? 4 : 0)
                + (ctx.isAnimationUpdateRequired() ? 3 : 0)
                + (ctx.isForcedChatUpdateRequired() ? npc.getForceChatText().length() + 1 : 0)
                + (ctx.isInteractingNpcUpdateRequired() ? 2 : 0)
                + (ctx.isFaceCoordinatesUpdateRequired() ? 4 : 0)
                + (ctx.isPrimaryHitUpdateRequired() ? 4 : 0)
                + (ctx.isSecondaryHitUpdateRequired() ? 4 : 0)
                + (ctx.isNpcDefinitionUpdateRequired() ? 2 : 0);
    }

    /**
     * Adds the NPC to the client-side local list.
     *
     * @param player The player
     * @param npc    The NPC being added
     */
    private static void addNpc(StreamBuffer.WriteBuffer out, Player player, Npc npc) {
        out.writeBits(14, npc.getSlot());
        Position delta = Position.delta(player.getPosition(), npc.getPosition());
        out.writeBits(5, delta.getY());
        out.writeBits(5, delta.getX());
        out.writeBit(npc.getUpdateContext().isUpdateRequired());
        out.writeBits(12, npc.getId());
        out.writeBit(true);
    }

    /**
     * Updates the movement of a NPC for this tick.
     *
     * @param npc The NPC to update
     */
    private static void updateNpcMovement(StreamBuffer.WriteBuffer out, Npc npc) {
        if (npc.getPrimaryDirection() == -1) {
            if (npc.getUpdateContext().isUpdateRequired()) {
                out.writeBit(true);
                out.writeBits(2, 0);
            } else {
                out.writeBit(false);
            }
        } else {
            out.writeBit(true);
            out.writeBits(2, 1);
            out.writeBits(3, npc.getPrimaryDirection());
            out.writeBit(npc.getUpdateContext().isUpdateRequired());
        }
    }

    /**
     * Updates the state of the NPC to the given update block.
     *
     * @param npc   The NPC to update
     */
    private static void updateState(StreamBuffer.WriteBuffer block, Npc npc) {
        NpcUpdateContext ctx = npc.getUpdateContext();

        // First we must calculate and write the mask.
        int mask = ctx.mask();

        if (mask >= 0x100) {
            mask |= 0x40;
            block.writeShort(mask, StreamBuffer.ByteOrder.LITTLE);
        } else {
            block.writeByte(mask);
        }

        // Finally, we append the attributes blocks.
        // Animation
        if (ctx.isAnimationUpdateRequired()) {
            appendAnimation(npc, block);
        }

        // Secondary hit
        if (ctx.isSecondaryHitUpdateRequired()) {
            appendSecondaryHit(npc, block);
        }

        // Graphics
        if (ctx.isGraphicsUpdateRequired()) {
            appendGraphic(npc, block);
        }

        // Interacting with npc
        if (ctx.isInteractingNpcUpdateRequired()) {
            appendInteractingNpc(npc, block);
        }

        // Forced chat
        if (ctx.isForcedChatUpdateRequired()) {
            appendForcedChat(npc, block);
        }

        // Primary hit
        if (ctx.isPrimaryHitUpdateRequired()) {
            appendPrimaryHit(npc, block);
        }

        // Npc definition
        if (ctx.isNpcDefinitionUpdateRequired()) {
            appendNpcDefinition(npc, block);
        }

        // Face coordinates
        if (ctx.isFaceCoordinatesUpdateRequired()) {
            appendFaceCoordinates(npc, block);
        }
    }

    /**
     * Appends the state of a player's animation to a buffer.
     */
    private static void appendAnimation(Npc npc, StreamBuffer.WriteBuffer out) {
        out.writeShort(npc.getAnimation().getId(), StreamBuffer.ByteOrder.LITTLE);
        out.writeByte(npc.getAnimation().getDelay());
    }

    /**
     * Append secondary hit to a buffer.
     */
    private static void appendSecondaryHit(Npc npc, StreamBuffer.WriteBuffer out) {
        out.writeByte(npc.getPrimaryHit().getDamage(), StreamBuffer.ValueType.A);
        out.writeByte(npc.getPrimaryHit().getType(), StreamBuffer.ValueType.C);
        out.writeByte(npc.getCurrentHealth(), StreamBuffer.ValueType.A);
        out.writeByte(npc.getMaximumHealth());
    }

    /**
     * Appends the state of a player's attached graphics to a buffer.
     */
    private static void appendGraphic(Npc npc, StreamBuffer.WriteBuffer out) {
        out.writeShort(npc.getGraphics().getId());
        out.writeInt(npc.getGraphics().getDelay());
    }

    /**
     * Append npc being interacted with to a buffer.
     */
    private static void appendInteractingNpc(Npc npc, StreamBuffer.WriteBuffer out) {
        out.writeShort(npc.getInteractingNpc().getId());
    }

    /**
     * Appends the state of a player's forced chat to a buffer.
     */
    private static void appendForcedChat(Npc npc, StreamBuffer.WriteBuffer out) {
        out.writeString(npc.getForceChatText());
    }

    /**
     * Append primary hit to a buffer.
     */
    private static void appendPrimaryHit(Npc npc, StreamBuffer.WriteBuffer out) {
        out.writeByte(npc.getPrimaryHit().getDamage(), StreamBuffer.ValueType.C);
        out.writeByte(npc.getPrimaryHit().getType(), StreamBuffer.ValueType.S);
        out.writeByte(npc.getCurrentHealth(), StreamBuffer.ValueType.S);
        out.writeByte(npc.getMaximumHealth(), StreamBuffer.ValueType.C);
    }

    /**
     * Append npc definition to a buffer.
     */
    private static void appendNpcDefinition(Npc npc, StreamBuffer.WriteBuffer out) {
        out.writeShort(npc.getNpcDefinitionId(), StreamBuffer.ValueType.A, StreamBuffer.ByteOrder.LITTLE);
    }

    /**
     * Append coordinates being faced to a buffer.
     */
    private static void appendFaceCoordinates(Npc npc, StreamBuffer.WriteBuffer out) {
        out.writeShort(npc.getFacingPosition().getX(), StreamBuffer.ByteOrder.LITTLE);
        out.writeShort(npc.getFacingPosition().getY(), StreamBuffer.ByteOrder.LITTLE);
    }

}
