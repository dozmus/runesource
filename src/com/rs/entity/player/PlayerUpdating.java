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

import com.rs.WorldHandler;
import com.rs.entity.Position;
import com.rs.entity.action.AsyncMovement;
import com.rs.entity.action.PublicChat;
import com.rs.net.StreamBuffer;
import com.rs.util.EquipmentHelper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides static utility methods for updating players.
 *
 * @author blakeman8192
 */
public final class PlayerUpdating {

    /**
     * The maximum length of the update appearance block.
     */
    private static final int MAX_APPEARANCE_BUFFER_SIZE = 56;
    /**
     * Regional player limit.
     */
    private static final int REGION_PLAYERS_LIMIT = 255;

    /**
     * Updates the player.
     */
    public static void update(Player player) {
        // TODO prioritise updating players based on distance to you
        // Remove disconnected players from local players list
        Iterator<Player> others = player.getPlayers().iterator();

        while (others.hasNext()) {
            Player next = others.next();

            if (next.getConnectionStage() == Client.ConnectionStage.LOGGED_OUT || next.isHidden()) {
                others.remove();
            }
        }

        // Find other players in local region
        List<Player> regionalPlayers = PlayerUpdating.updateLocalPlayers(player);

        // Calculate block buffer size
        int baseBlockSize = 128;
        int stateBlockSize = PlayerUpdating.stateBlockSize(player, true);

        for (Player other : player.getPlayers()) {
            if (player.updatableForPlayer(other)) {
                baseBlockSize += 3; // up to 19 bits for movement
                stateBlockSize += PlayerUpdating.stateBlockSize(other, true);
            }
        }

        for (Player other : regionalPlayers) {
            baseBlockSize += 3; // 23 bits for add player
            stateBlockSize += PlayerUpdating.stateBlockSize(other, false);
        }

        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(baseBlockSize + stateBlockSize);
        StreamBuffer.WriteBuffer stateBlock = StreamBuffer.createWriteBuffer(stateBlockSize);

        // Initialize the update packet.
        out.writeVariableShortHeader(player.getEncryptor(), 81);
        out.setAccessType(StreamBuffer.AccessType.BIT_ACCESS);

        // Update this player.
        PlayerUpdating.updateLocalPlayerMovement(player, out);

        if (player.getUpdateContext().isUpdateRequired()) {
            PlayerUpdating.updateState(player, stateBlock, false, true);
        }

        // Update other local players.
        out.writeBits(8, player.getPlayers().size());

        for (Iterator<Player> i = player.getPlayers().iterator(); i.hasNext(); ) {
            Player other = i.next();

            if (player.updatableForPlayer(other)) {
                PlayerUpdating.updateOtherPlayerMovement(other, out);

                if (other.getUpdateContext().isUpdateRequired()) {
                    boolean ignored = player.getAttributes().isIgnored(other.getUsername())
                            && other.getAttributes().getPrivilege() == Player.Privilege.REGULAR;
                    PlayerUpdating.updateState(other, stateBlock, false, ignored);
                }
            } else {
                out.writeBit(true);
                out.writeBits(2, 3);
                i.remove();
            }
        }

        // Update the local player list.
        for (Player other : regionalPlayers) {
            boolean ignored = player.getAttributes().isIgnored(other.getUsername())
                    && other.getAttributes().getPrivilege() == Player.Privilege.REGULAR;
            player.getPlayers().add(other);
            PlayerUpdating.addPlayer(out, player, other);
            PlayerUpdating.updateState(other, stateBlock, true, ignored);
        }

        // Append the attributes block to the main packet.
        if (stateBlock.getBuffer().position() > 0) {
            out.writeBits(11, 2047);
            out.setAccessType(StreamBuffer.AccessType.BYTE_ACCESS);
            out.writeBytes(stateBlock.getBuffer());
        } else {
            out.setAccessType(StreamBuffer.AccessType.BYTE_ACCESS);
        }

        // Finish the packet and send it.
        out.finishVariableShortHeader();
        player.send(out.getBuffer());
    }

    /**
     * Update local player list for the given player.
     */
    private static List<Player> updateLocalPlayers(Player player) {
        List<Player> players = new ArrayList<>();

        for (Player other : WorldHandler.getInstance().getPlayers()) {
            if (player.getPlayers().size() + players.size() >= REGION_PLAYERS_LIMIT) {
                break; // Local player limit has been reached.
            }

            if (other == null || other == player || other.getConnectionStage() != Client.ConnectionStage.LOGGED_IN
                    || player.getPlayers().contains(other) || other.isHidden()) {
                continue;
            }

            if (other.getPosition().isViewableFrom(player.getPosition())) {
                players.add(other);
            }
        }
        return players;
    }

    /**
     * The size in bytes of the state block for the given player.
     */
    private static int stateBlockSize(Player player, boolean checkIfUpdateRequired) {
        PlayerUpdateContext ctx = player.getUpdateContext();

        if (checkIfUpdateRequired && !ctx.isUpdateRequired())
            return 0;
        return 2 + MAX_APPEARANCE_BUFFER_SIZE
                + (ctx.isAsyncMovementUpdateRequired() ? 9 : 0)
                + (ctx.isGraphicsUpdateRequired() ? 6 : 0)
                + (ctx.isAnimationUpdateRequired() ? 3 : 0)
                + (ctx.isForcedChatUpdateRequired() ? player.getForceChatText().length() + 1 : 0)
                + (ctx.isPublicChatUpdateRequired() ? player.getPublicChat().getText().length + 4 : 0)
                + (ctx.isInteractingNpcUpdateRequired() ? 2 : 0)
                + (ctx.isFaceCoordinatesUpdateRequired() ? 4 : 0)
                + (ctx.isPrimaryHitUpdateRequired() ? 4 : 0)
                + (ctx.isSecondaryHitUpdateRequired() ? 4 : 0);
    }

    /**
     * Appends the state of a player's appearance to a buffer.
     *
     * @param player the player
     */
    private static void appendAppearance(Player player, StreamBuffer.WriteBuffer out) {
        PlayerAttributes attributes = player.getAttributes();
        StreamBuffer.WriteBuffer block = StreamBuffer.createWriteBuffer(MAX_APPEARANCE_BUFFER_SIZE);

        block.writeByte(player.getAttributes().getGender()); // Gender
        block.writeByte(0); // Skull icon

        // Player models
        int[] e = attributes.getEquipment();
        int[] a = attributes.getAppearance();

        // Hat.
        if (e[EquipmentHelper.EQUIPMENT_SLOT_HEAD] > 1) {
            block.writeShort(0x200 + e[EquipmentHelper.EQUIPMENT_SLOT_HEAD]);
        } else {
            block.writeByte(0);
        }

        // Cape.
        if (e[EquipmentHelper.EQUIPMENT_SLOT_CAPE] > 1) {
            block.writeShort(0x200 + e[EquipmentHelper.EQUIPMENT_SLOT_CAPE]);
        } else {
            block.writeByte(0);
        }

        // Amulet.
        if (e[EquipmentHelper.EQUIPMENT_SLOT_AMULET] > 1) {
            block.writeShort(0x200 + e[EquipmentHelper.EQUIPMENT_SLOT_AMULET]);
        } else {
            block.writeByte(0);
        }

        // Weapon.
        if (e[EquipmentHelper.EQUIPMENT_SLOT_WEAPON] > 1) {
            block.writeShort(0x200 + e[EquipmentHelper.EQUIPMENT_SLOT_WEAPON]);
        } else {
            block.writeByte(0);
        }

        // Chest.
        if (e[EquipmentHelper.EQUIPMENT_SLOT_CHEST] > 1) {
            block.writeShort(0x200 + e[EquipmentHelper.EQUIPMENT_SLOT_CHEST]);
        } else {
            block.writeShort(0x100 + a[EquipmentHelper.APPEARANCE_SLOT_CHEST]);
        }

        // Shield.
        if (e[EquipmentHelper.EQUIPMENT_SLOT_SHIELD] > 1) {
            block.writeShort(0x200 + e[EquipmentHelper.EQUIPMENT_SLOT_SHIELD]);
        } else {
            block.writeByte(0);
        }

        // Arms TODO: Check platebody/non-platebody.
        if (e[EquipmentHelper.EQUIPMENT_SLOT_CHEST] > 1) {
            block.writeShort(0x200 + e[EquipmentHelper.EQUIPMENT_SLOT_CHEST]);
        } else {
            block.writeShort(0x100 + a[EquipmentHelper.APPEARANCE_SLOT_ARMS]);
        }

        // Legs.
        if (e[EquipmentHelper.EQUIPMENT_SLOT_LEGS] > 1) {
            block.writeShort(0x200 + e[EquipmentHelper.EQUIPMENT_SLOT_LEGS]);
        } else {
            block.writeShort(0x100 + a[EquipmentHelper.APPEARANCE_SLOT_LEGS]);
        }

        // Head (with a hat already on).
        if (EquipmentHelper.isFullHelm(e[EquipmentHelper.EQUIPMENT_SLOT_HEAD])
                || EquipmentHelper.isFullMask(EquipmentHelper.EQUIPMENT_SLOT_HEAD)) {
            block.writeByte(0);
        } else {
            block.writeShort(0x100 + a[EquipmentHelper.APPEARANCE_SLOT_HEAD]);
        }

        // Hands.
        if (e[EquipmentHelper.EQUIPMENT_SLOT_HANDS] > 1) {
            block.writeShort(0x200 + e[EquipmentHelper.EQUIPMENT_SLOT_HANDS]);
        } else {
            block.writeShort(0x100 + a[EquipmentHelper.APPEARANCE_SLOT_HANDS]);
        }

        // Feet.
        if (e[EquipmentHelper.EQUIPMENT_SLOT_FEET] > 1) {
            block.writeShort(0x200 + e[EquipmentHelper.EQUIPMENT_SLOT_FEET]);
        } else {
            block.writeShort(0x100 + a[EquipmentHelper.APPEARANCE_SLOT_FEET]);
        }

        // Beard.
        if (EquipmentHelper.isFullHelm(e[EquipmentHelper.EQUIPMENT_SLOT_HEAD])
                || EquipmentHelper.isFullMask(EquipmentHelper.EQUIPMENT_SLOT_HEAD)) {
            block.writeByte(0);
        } else {
            block.writeShort(0x100 + a[EquipmentHelper.APPEARANCE_SLOT_BEARD]);
        }

        // Player colors
        for (int color : attributes.getColors()) {
            block.writeByte(color);
        }

        // Movement animations
        block.writeShort(0x328); // stand
        block.writeShort(0x337); // stand turn
        block.writeShort(0x333); // walk
        block.writeShort(0x334); // turn 180
        block.writeShort(0x335); // turn 90 cw
        block.writeShort(0x336); // turn 90 ccw
        block.writeShort(0x338); // run

        block.writeLong(player.getUsername());
        block.writeByte(attributes.getCombatLevel());
        block.writeShort(attributes.getTotalLevel());

        // Append the block length and the block to the packet.
        out.writeByte(block.getBuffer().position(), StreamBuffer.ValueType.C);
        out.writeBytes(block.getBuffer());
    }

    /**
     * Adds a player to the local player list of another player.
     *
     * @param out    the packet to write to
     * @param player the host player
     * @param other  the player being added
     */
    private static void addPlayer(StreamBuffer.WriteBuffer out, Player player, Player other) {
        out.writeBits(11, other.getSlot()); // Server slot.
        out.writeBit(true); // Yes, an update is required.
        out.writeBit(true); // Discard walking queue(?)

        // Write the relative position.
        Position delta = Position.delta(player.getPosition(), other.getPosition());
        out.writeBits(5, delta.getY());
        out.writeBits(5, delta.getX());
    }

    /**
     * Updates movement for this local player. The difference between this
     * method and the other player method is that this will make use of sector
     * 2,3 to place the player in a specific position while sector 2,3 is not
     * present in updating of other players (it simply flags local list removal
     * instead).
     */
    private static void updateLocalPlayerMovement(Player player, StreamBuffer.WriteBuffer out) {
        boolean updateRequired = player.getUpdateContext().isUpdateRequired();

        if (player.needsPlacement()) { // Do they need placement?
            out.writeBit(true); // Yes, there is an update.
            int posX = player.getPosition().getLocalX(player.getCurrentRegion());
            int posY = player.getPosition().getLocalY(player.getCurrentRegion());
            appendPlacement(out, posX, posY, player.getPosition().getZ(), player.isResetMovementQueue(), updateRequired);
        } else { // No placement update, check for movement.
            int pDir = player.getPrimaryDirection();
            int sDir = player.getSecondaryDirection();
            updateMovement(out, pDir, sDir, updateRequired);
        }
    }

    /**
     * Updates the movement of a player for another player (does not make use of sector 2, 3).
     */
    private static void updateOtherPlayerMovement(Player player, StreamBuffer.WriteBuffer out) {
        boolean updateRequired = player.getUpdateContext().isUpdateRequired();
        int pDir = player.getPrimaryDirection();
        int sDir = player.getSecondaryDirection();
        updateMovement(out, pDir, sDir, updateRequired);
    }

    private static void updateMovement(StreamBuffer.WriteBuffer out, int pDir, int sDir, boolean updateRequired) {
        if (pDir != -1) { // If they moved.
            out.writeBit(true); // Yes, there is an update.

            if (sDir != -1) { // If they ran.
                appendRun(out, pDir, sDir, updateRequired);
            } else { // Movement but no running - they walked.
                appendWalk(out, pDir, updateRequired);
            }
        } else { // No movement.
            if (updateRequired) { // Does the state need to be updated?
                out.writeBit(true); // Yes, there is an update.
                appendStand(out);
            } else { // No update whatsoever.
                out.writeBit(false);
            }
        }
    }

    /**
     * Updates the flag-based state of a player.
     */
    private static void updateState(Player player, StreamBuffer.WriteBuffer block, boolean forceAppearance,
                                   boolean noPublicChat) {
        PlayerUpdateContext ctx = player.getUpdateContext();

        // Check if the result is cached
        ByteBuffer buffer = ctx.getBuffer(forceAppearance, noPublicChat);

        if (buffer != null) {
            block.writeBytes(buffer);
            return;
        }

        // Create new result and cache it
        StreamBuffer.WriteBuffer result = StreamBuffer.createWriteBuffer(stateBlockSize(player, false));

        // First we must calculate and write the mask.
        int mask = ctx.mask(forceAppearance, noPublicChat);

        if (mask >= 0x100) {
            mask |= 0x40;
            result.writeShort(mask, StreamBuffer.ByteOrder.LITTLE);
        } else {
            result.writeByte(mask);
        }

        // Finally, we append the attributes blocks.
        // Async. walking
        if (ctx.isAsyncMovementUpdateRequired()) {
            appendAsyncMovement(player, result);
        }

        // Graphics
        if (ctx.isGraphicsUpdateRequired()) {
            appendGraphic(player, result);
        }

        // Animation
        if (ctx.isAnimationUpdateRequired()) {
            appendAnimation(player, result);
        }

        // Forced chat
        if (ctx.isForcedChatUpdateRequired()) {
            appendForcedChat(player, result);
        }

        // Chat
        if (ctx.isPublicChatUpdateRequired() && !noPublicChat) {
            appendPublicChat(player, result);
        }

        // Interacting with npc
        if (ctx.isInteractingNpcUpdateRequired()) {
            appendInteractingNpc(player, result);
        }

        // Appearance
        if (ctx.isAppearanceUpdateRequired() || forceAppearance) {
            appendAppearance(player, result);
        }

        // Face coordinates
        if (ctx.isFaceCoordinatesUpdateRequired()) {
            appendFaceCoordinates(player, result);
        }

        // Primary hit
        if (ctx.isPrimaryHitUpdateRequired()) {
            appendPrimaryHit(player, result);
        }

        // Secondary hit
        if (ctx.isSecondaryHitUpdateRequired()) {
            appendSecondaryHit(player, result);
        }

        // Cache and write the result
        block.writeBytes(result.getBuffer());
        ctx.setBuffer(forceAppearance, noPublicChat, result.getBuffer());
    }


    /**
     * Append coordinates being faced to a buffer.
     */
    private static void appendFaceCoordinates(Player player, StreamBuffer.WriteBuffer out) {
        out.writeShort(player.getFacingPosition().getX(), StreamBuffer.ValueType.A, StreamBuffer.ByteOrder.LITTLE);
        out.writeShort(player.getFacingPosition().getY(), StreamBuffer.ByteOrder.LITTLE);
    }

    /**
     * Append asynchronous movement to a buffer.
     */
    private static void appendAsyncMovement(Player player, StreamBuffer.WriteBuffer out) {
        AsyncMovement asyncMovement = player.getAsyncMovement();
        out.writeByte(asyncMovement.getStartPosition().getX(), StreamBuffer.ValueType.S);
        out.writeByte(asyncMovement.getStartPosition().getY(), StreamBuffer.ValueType.S);
        out.writeByte(asyncMovement.getEndPosition().getX(), StreamBuffer.ValueType.S);
        out.writeByte(asyncMovement.getEndPosition().getY(), StreamBuffer.ValueType.S);
        out.writeShort(asyncMovement.getStartToEndSpeed(), StreamBuffer.ValueType.A, StreamBuffer.ByteOrder.LITTLE);
        out.writeShort(asyncMovement.getEndToStartSpeed(), StreamBuffer.ValueType.A);
        out.writeByte(asyncMovement.getDirection(), StreamBuffer.ValueType.S);
    }

    /**
     * Append npc being interacted with to a buffer.
     */
    private static void appendInteractingNpc(Player player, StreamBuffer.WriteBuffer out) {
        out.writeShort(player.getInteractingNpc().getId(), StreamBuffer.ByteOrder.LITTLE);
    }

    /**
     * Append primary hit to a buffer.
     */
    private static void appendPrimaryHit(Player player, StreamBuffer.WriteBuffer out) {
        out.writeByte(player.getPrimaryHit().getDamage());
        out.writeByte(player.getPrimaryHit().getType(), StreamBuffer.ValueType.A);
        out.writeByte(player.getAttributes().getSkills()[3], StreamBuffer.ValueType.C);
        out.writeByte(player.getAttributes().getSkills()[3]); // TODO send maximum health
    }

    /**
     * Append secondary hit to a buffer.
     */
    private static void appendSecondaryHit(Player player, StreamBuffer.WriteBuffer out) {
        out.writeByte(player.getSecondaryHit().getDamage());
        out.writeByte(player.getSecondaryHit().getType(), StreamBuffer.ValueType.A);
        out.writeByte(player.getAttributes().getSkills()[3], StreamBuffer.ValueType.C);
        out.writeByte(player.getAttributes().getSkills()[3]); // TODO send maximum health
    }

    /**
     * Appends the state of a player's forced chat to a buffer.
     */
    private static void appendForcedChat(Player player, StreamBuffer.WriteBuffer out) {
        out.writeString(player.getForceChatText());
    }

    /**
     * Appends the state of a player's public chat to a buffer.
     */
    private static void appendPublicChat(Player player, StreamBuffer.WriteBuffer out) {
        PublicChat chat = player.getPublicChat();
        out.writeShort(((chat.getColor() & 0xff) << 8) + (chat.getEffects() & 0xff), StreamBuffer.ByteOrder.LITTLE);
        out.writeByte(player.getAttributes().getPrivilege().toInt());
        out.writeByte(chat.getText().length, StreamBuffer.ValueType.C);
        out.writeBytesReverse(chat.getText());
    }

    /**
     * Appends the state of a player's attached graphics to a buffer.
     */
    private static void appendGraphic(Player player, StreamBuffer.WriteBuffer out) {
        out.writeShort(player.getGraphics().getId(), StreamBuffer.ByteOrder.LITTLE);
        out.writeInt(player.getGraphics().getDelay());
    }

    /**
     * Appends the state of a player's animation to a buffer.
     */
    private static void appendAnimation(Player player, StreamBuffer.WriteBuffer out) {
        out.writeShort(player.getAnimation().getId(), StreamBuffer.ByteOrder.LITTLE);
        out.writeByte(player.getAnimation().getDelay(), StreamBuffer.ValueType.C);
    }

    /**
     * Appends the stand version of the movement section of the update packet
     * (sector 2,0). Appending this (instead of just a zero bit) automatically
     * assumes that there is a required attribute update afterwards.
     */
    private static void appendStand(StreamBuffer.WriteBuffer out) {
        out.writeBits(2, 0); // 0 - no movement.
    }

    /**
     * Appends the walk version of the movement section of the update packet
     * (sector 2,1).
     *
     * @param out              the buffer to append to
     * @param direction        the walking direction
     * @param attributesUpdate whether or not a player attributes update is required
     */
    private static void appendWalk(StreamBuffer.WriteBuffer out, int direction, boolean attributesUpdate) {
        out.writeBits(2, 1); // 1 - walking.

        // Append the actual sector.
        out.writeBits(3, direction);
        out.writeBit(attributesUpdate);
    }

    /**
     * Appends the walk version of the movement section of the update packet (sector 2,2).
     *
     * @param direction        the walking direction
     * @param direction2       the running direction
     * @param attributesUpdate whether or not a player attributes update is required
     */
    private static void appendRun(StreamBuffer.WriteBuffer out, int direction, int direction2, boolean attributesUpdate) {
        out.writeBits(2, 2); // 2 - running.

        // Append the actual sector.
        out.writeBits(3, direction);
        out.writeBits(3, direction2);
        out.writeBit(attributesUpdate);
    }

    /**
     * Appends the player placement version of the movement section of the update packet (sector 2,3).
     * Note that by others this was previously called the "teleport update".
     *
     * @param localX               the local X coordinate
     * @param localY               the local Y coordinate
     * @param z                    the Z coordinate
     * @param discardMovementQueue whether or not the client should discard the movement queue
     * @param attributesUpdate     whether or not a plater attributes update is required
     */
    private static void appendPlacement(StreamBuffer.WriteBuffer out, int localX, int localY, int z,
                                        boolean discardMovementQueue, boolean attributesUpdate) {
        out.writeBits(2, 3); // 3 - placement.

        // Append the actual sector.
        out.writeBits(2, z);
        out.writeBit(discardMovementQueue);
        out.writeBit(attributesUpdate);
        out.writeBits(7, localY);
        out.writeBits(7, localX);
    }

}
