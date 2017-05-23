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

import com.rs.entity.Position;
import com.rs.entity.player.Player;
import com.rs.WorldHandler;
import com.rs.net.StreamBuffer;

import java.util.Iterator;

/**
 * Provides static utility methods for updating NPCs.
 *
 * @author blakeman8192
 */
public final class NpcUpdating {

    /**
     * Updates all NPCs for the argued Player.
     *
     * @param player the player
     */
    public static void update(Player player) {
        // XXX: The buffer sizes may need to be tuned.
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(2048);
        StreamBuffer.WriteBuffer block = StreamBuffer.createWriteBuffer(1024);

        // Initialize the update packet.
        out.writeVariableShortPacketHeader(player.getEncryptor(), 65);
        out.setAccessType(StreamBuffer.AccessType.BIT_ACCESS);

        // Update the NPCs in the local list.
        out.writeBits(8, player.getNpcs().size());

        for (Iterator<Npc> i = player.getNpcs().iterator(); i.hasNext(); ) {
            Npc npc = i.next();

            if (npc.getPosition().isViewableFrom(player.getPosition()) && npc.isVisible()) {
                NpcUpdating.updateNpcMovement(out, npc);

                if (npc.isUpdateRequired()) {
                    NpcUpdating.updateState(block, npc);
                }
            } else {
                // Remove the NPC from the local list.
                out.writeBit(true);
                out.writeBits(2, 3);
                i.remove();
            }
        }

        // Update the local NPC list itself.
        for (int i = 0; i < WorldHandler.getInstance().getNpcs().length; i++) {
            Npc npc = WorldHandler.getInstance().getNpcs()[i];

            if (npc == null || player.getNpcs().contains(npc) || !npc.isVisible()) {
                continue;
            }

            if (npc.getPosition().isViewableFrom(player.getPosition())) {
                addNpc(out, player, npc);

                if (npc.isUpdateRequired()) {
                    NpcUpdating.updateState(block, npc);
                }
            }
        }

        // Append the update block to the packet if need be.
        if (block.getBuffer().position() > 0) {
            out.writeBits(14, 16383);
            out.setAccessType(StreamBuffer.AccessType.BYTE_ACCESS);
            out.writeBytes(block.getBuffer());
        } else {
            out.setAccessType(StreamBuffer.AccessType.BYTE_ACCESS);
        }

        // Ship the packet out to the client.
        out.finishVariableShortPacketHeader();
        player.send(out.getBuffer());
    }

    /**
     * Adds the NPC to the clientside local list.
     *
     * @param out    The buffer to write to
     * @param player The player
     * @param npc    The NPC being added
     */
    private static void addNpc(StreamBuffer.WriteBuffer out, Player player, Npc npc) {
        out.writeBits(14, npc.getSlot());
        Position delta = Position.delta(player.getPosition(), npc.getPosition());
        out.writeBits(5, delta.getY());
        out.writeBits(5, delta.getX());
        out.writeBit(npc.isUpdateRequired());
        out.writeBits(12, npc.getNpcId());
        out.writeBit(true);
    }

    /**
     * Updates the movement of a NPC for this tick.
     *
     * @param out The buffer to write to
     * @param npc The NPC to update
     */
    private static void updateNpcMovement(StreamBuffer.WriteBuffer out, Npc npc) {
        if (npc.getPrimaryDirection() == -1) {
            if (npc.isUpdateRequired()) {
                out.writeBit(true);
                out.writeBits(2, 0);
            } else {
                out.writeBit(false);
            }
        } else {
            out.writeBit(true);
            out.writeBits(2, 1);
            out.writeBits(3, npc.getPrimaryDirection());
            out.writeBit(true);
        }
    }

    /**
     * Updates the state of the NPC to the given update block.
     *
     * @param block The update block to append to
     * @param npc   The NPC to update
     */
    private static void updateState(StreamBuffer.WriteBuffer block, Npc npc) {
        int mask = 0x0;

        // TODO: NPC update masks.

        if (mask >= 0x100) {
            mask |= 0x40;
            block.writeShort(mask, StreamBuffer.ByteOrder.LITTLE);
        } else {
            block.writeByte(mask);
        }

        // TODO: Append the NPC update blocks.
    }

}
