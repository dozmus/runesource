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

import com.rs.Server;
import com.rs.entity.Entity;
import com.rs.entity.Position;
import com.rs.entity.action.PublicChat;
import com.rs.net.HostGateway;
import com.rs.net.ISAACCipher;
import com.rs.net.StreamBuffer;
import com.rs.plugin.Bootstrap;
import com.rs.plugin.PluginHandler;
import com.rs.task.TaskHandler;
import com.rs.util.EquipmentHelper;
import com.rs.util.Misc;
import com.rs.util.WeaponDefinition;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

/**
 * The class behind a Player that handles all networking-related things.
 *
 * @author blakeman8192
 */
public abstract class Client extends Entity {

    /**
     * Lengths for the various packets.
     */
    public static final int[] PACKET_LENGTHS = {
            0, 0, 0, 1, -1, 0, 0, 0, 0, 0, // 0
            0, 0, 0, 0, 8, 0, 6, 2, 2, 0, // 10
            0, 2, 0, 6, 0, 12, 0, 0, 0, 0, // 20
            0, 0, 0, 0, 0, 8, 4, 0, 0, 2, // 30
            2, 6, 0, 6, 0, -1, 0, 0, 0, 0, // 40
            0, 0, 0, 12, 0, 0, 0, 0, 8, 0, // 50
            0, 8, 0, 0, 0, 0, 0, 0, 0, 0, // 60
            6, 0, 2, 2, 8, 6, 0, -1, 0, 6, // 70
            0, 0, 0, 0, 0, 1, 4, 6, 0, 0, // 80
            0, 0, 0, 0, 0, 3, 0, 0, -1, 0, // 90
            0, 13, 0, -1, 0, 0, 0, 0, 0, 0,// 100
            0, 0, 0, 0, 0, 0, 0, 6, 0, 0, // 110
            1, 0, 6, 0, 0, 0, -1, 0, 2, 6, // 120
            0, 4, 6, 8, 0, 6, 0, 0, 0, 2, // 130
            0, 0, 0, 0, 0, 6, 0, 0, 0, 0, // 140
            0, 0, 1, 2, 0, 2, 6, 0, 0, 0, // 150
            0, 0, 0, 0, -1, -1, 0, 0, 0, 0,// 160
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 170
            0, 8, 0, 3, 0, 2, 0, 0, 8, 1, // 180
            0, 0, 12, 0, 0, 0, 0, 0, 0, 0, // 190
            2, 0, 0, 0, 0, 0, 0, 0, 4, 0, // 200
            4, 0, 0, 0, 7, 8, 0, 0, 10, 0, // 210
            0, 0, 0, 0, 0, 0, -1, 0, 6, 0, // 220
            1, 0, 0, 0, 6, 0, 6, 8, 1, 0, // 230
            0, 4, 0, 0, 0, 0, -1, 0, -1, 4,// 240
            0, 0, 6, 6, 0, 0, 0 // 250
    };
    private final SelectionKey key;
    private final ByteBuffer inData;
    private final Player player = (Player) this;
    private final Misc.Stopwatch timeoutStopwatch = new Misc.Stopwatch();
    private SocketChannel socketChannel;
    private ConnectionStage connectionStage;
    private int packetOpcode = -1;
    private int packetLength = -1;
    private ISAACCipher encryptor;
    private ISAACCipher decryptor;

    /**
     * Creates a new Client.
     *
     * @param key the SelectionKey of the client
     */
    public Client(SelectionKey key) {
        super(new PlayerUpdateContext());
        this.key = key;
        inData = ByteBuffer.allocateDirect(512);
        setConnectionStage(ConnectionStage.CONNECTED);

        if (key != null)
            socketChannel = (SocketChannel) key.channel();
    }

    /**
     * Called after the player finishes logging in.
     */
    public abstract void login(String username, String password) throws Exception;

    /**
     * Called before the player disconnects.
     */
    public abstract void logout() throws Exception;

    /**
     * Sends all skills to the client.
     */
    public void sendSkills() {
        for (int i = 0; i < player.getAttributes().getSkills().length; i++) {
            sendSkill(i, player.getAttributes().getSkills()[i], player.getAttributes().getExperience()[i]);
        }
    }

    /**
     * Sends all equipment.
     */
    public void sendEquipment() {
        for (int i = 0; i < player.getAttributes().getEquipment().length; i++) {
            sendEquipment(i, player.getAttributes().getEquipment()[i], player.getAttributes().getEquipmentN()[i]);
        }
    }

    /**
     * Sends the skill to the client.
     *
     * @param skillId the skill ID
     * @param level   the skill level
     * @param exp     the skill experience
     */
    public void sendSkill(int skillId, int level, int exp) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(7);
        out.writeHeader(getEncryptor(), 134);
        out.writeByte(skillId);
        out.writeInt(exp, StreamBuffer.ByteOrder.MIDDLE);
        out.writeByte(level);
        send(out.getBuffer());
    }

    /**
     * Sends the equipment to the client.
     *
     * @param slot       the equipment slot
     * @param itemId     the item ID
     * @param itemAmount the item amount
     */
    public void sendEquipment(int slot, int itemId, int itemAmount) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(13);
        out.writeVariableShortHeader(getEncryptor(), 34);
        out.writeShort(1688);
        out.writeByte(slot);
        out.writeShort(itemId + 1);

        if (itemAmount > 254) {
            out.writeByte(255);
            out.writeInt(itemAmount);
        } else {
            out.writeByte(itemAmount);
        }
        out.finishVariableShortHeader();
        send(out.getBuffer());
    }

    /**
     * Sends the current inventory.
     */
    public void sendInventory() {
        int[] inv = player.getAttributes().getInventory();
        int[] invN =  player.getAttributes().getInventoryN();
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(10 + inv.length*7);
        out.writeVariableShortHeader(getEncryptor(), 53);
        out.writeShort(3214);
        out.writeShort(inv.length);

        for (int i = 0; i < inv.length; i++) {
            if (invN[i] > 254) {
                out.writeByte(255);
                out.writeInt(invN[i], StreamBuffer.ByteOrder.INVERSE_MIDDLE);
            } else {
                out.writeByte(invN[i]);
            }
            out.writeShort(inv[i] + 1, StreamBuffer.ValueType.A, StreamBuffer.ByteOrder.LITTLE);
        }
        out.finishVariableShortHeader();
        send(out.getBuffer());
    }

    /**
     * Sends a message to the players chat box.
     *
     * @param message the message
     */
    public void sendMessage(String message) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(message.length() + 3);
        out.writeVariableHeader(getEncryptor(), 253);
        out.writeString(message);
        out.finishVariableHeader();
        send(out.getBuffer());
    }

    /**
     * Sends a sidebar interface.
     *
     * @param menuId the interface slot
     * @param form   the interface ID
     */
    public void sendSidebarInterface(int menuId, int form) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(4);
        out.writeHeader(getEncryptor(), 71);
        out.writeShort(form);
        out.writeByte(menuId, StreamBuffer.ValueType.A);
        send(out.getBuffer());
    }

    /**
     * Sends and refreshes the map region.
     */
    public void sendMapRegion() {
        player.getCurrentRegion().setAs(player.getPosition());
        player.setNeedsPlacement(true);
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(5);
        out.writeHeader(getEncryptor(), 73);
        out.writeShort(player.getPosition().getRegionX() + 6, StreamBuffer.ValueType.A);
        out.writeShort(player.getPosition().getRegionY() + 6);
        send(out.getBuffer());
    }

    /**
     * Sends your run energy to the client.
     */
    public void sendRunEnergy() {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(2);
        out.writeHeader(getEncryptor(), 110);
        out.writeByte((int)player.getAttributes().getRunEnergy());
        send(out.getBuffer());
    }

    /**
     * Sends the friends list status to the player.
     * @param status 0 is loading, 1 is connecting, 2 is loaded.
     */
    public void sendFriendsListStatus(int status) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(2);
        out.writeHeader(getEncryptor(), 221);
        out.writeByte(status);
        send(out.getBuffer());
    }

    /**
     * Sends an added friend and the world they're on (9 + N for world N or 0 for offline).
     */
    public void sendAddFriend(long name, int worldNo) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(10);
        out.writeHeader(getEncryptor(), 50);
        out.writeLong(name);
        out.writeByte(worldNo);
        send(out.getBuffer());
    }

    /**
     * Sends ignored friends.
     */
    public void sendAddIgnores(Collection<Long> names) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(3 + names.size()*8);
        out.writeVariableShortHeader(getEncryptor(), 214);

        for (long name : names)
            out.writeLong(name);
        out.finishVariableShortHeader();
        send(out.getBuffer());
    }

    /**
     * Sends a private message.
     * @param name The sender.
     * @param messageCounter A server-wide count of messages sent.
     * @param privilege The sender's privileges.
     * @param text The message to send, encoded in ASCII.
     */
    public void sendPrivateMessage(long name, int messageCounter, Player.Privilege privilege, byte[] text) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(15 + text.length);
        out.writeVariableHeader(getEncryptor(), 196);
        out.writeLong(name);
        out.writeInt(messageCounter);
        out.writeByte(privilege.toInt());

        for (byte b : text)
            out.writeByte(b);
        out.finishVariableHeader();
        send(out.getBuffer());
    }

    /**
     * Shows the given interface on the client.
     * Make sure you also call {@link Player#setCurrentInterfaceId(int)} for packet injection avoiding purposes.
     */
    public void sendInterface(int interfaceId) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(3);
        out.writeHeader(getEncryptor(), 97);
        out.writeShort(interfaceId);
        send(out.getBuffer());
    }

    /**
     * Closes all interfaces open in the client.
     * Make sure you also call {@link Player#setCurrentInterfaceId(int)} with argument -1 for packet injection avoiding
     * purposes.
     */
    public void sendClearScreen() {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(1);
        out.writeHeader(getEncryptor(), 219);
        send(out.getBuffer());
    }

    /**
     * Changes a client-side setting to the specified value.
     */
    public void sendClientSetting(int settingId, int value) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(4);
        out.writeHeader(getEncryptor(), 36);
        out.writeShort(settingId, StreamBuffer.ByteOrder.LITTLE);
        out.writeByte(value);
        send(out.getBuffer());
    }

    /**
     * Sends a message telling the client to log out.
     */
    public void sendLogout() {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(1);
        out.writeHeader(getEncryptor(), 109);
        send(out.getBuffer());
    }

    /**
     * Sets the text shown on an interface.
     */
    public void sendInterfaceText(int interfaceId, String text) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(6 + text.length());
        out.writeVariableShortHeader(getEncryptor(), 126);
        out.writeString(text);
        out.writeShort(interfaceId, StreamBuffer.ValueType.A);
        out.finishVariableShortHeader();
        send(out.getBuffer());
    }

    /**
     * Sets the item shown on an interface.
     */
    public void sendInterfaceItem(int interfaceId, int itemId, int zoom) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(7);
        out.writeHeader(getEncryptor(), 246);
        out.writeShort(interfaceId, StreamBuffer.ByteOrder.LITTLE);
        out.writeShort(zoom);
        out.writeShort(itemId);
        send(out.getBuffer());
    }

    /**
     * Sends a packet that tells the client to reset all button states.
     */
    public void sendResetAllButtonStates() {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(1);
        out.writeHeader(getEncryptor(), 68);
        send(out.getBuffer());
    }

    public void sendWeaponInterface() {
        int weaponId = player.getAttributes().getEquipment()[EquipmentHelper.EQUIPMENT_SLOT_WEAPON];
        WeaponDefinition def = EquipmentHelper.getWeaponDefinition(weaponId);
        int interfaceId = def.getType().getInterfaceId();

        // Send player weapon interface
        if (player.getCurrentWeaponInterfaceId() != interfaceId) {
            player.setCurrentWeaponInterfaceId(interfaceId);
            sendSidebarInterface(0, interfaceId);
        }

        // Send interface text and model
        if (def.getType().getWeaponNameId() != -1) {
            sendInterfaceText(def.getType().getWeaponNameId(), def.getName());
        }

        if (def.getType().getWeaponImageId() != -1) {
            sendInterfaceItem(def.getType().getWeaponImageId(), def.getId(), 150);
        }
    }

    /**
     * Sends chat filter modes.
     */
    public void sendChatModes(int publicChatMode, int privateChatMode, int tradeMode) {
        StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(4);
        out.writeHeader(getEncryptor(), 206);
        out.writeByte(publicChatMode);
        out.writeByte(privateChatMode);
        out.writeByte(tradeMode);
        send(out.getBuffer());
    }

    /**
     * Sends the currently active chat filter modes.
     */
    public void sendChatModes() {
        PlayerSettings settings = player.getAttributes().getSettings();
        sendChatModes(settings.getPublicChatMode(), settings.getPrivateChatMode(), settings.getTradeMode());
    }

    /**
     * Disconnects the client.
     */
    public void disconnect() {
        System.out.println(this + " disconnecting.");

        try {
            TaskHandler.remove(player);
            logout();
            getSocketChannel().close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            HostGateway.exit(getSocketChannel().socket().getInetAddress().getHostAddress());
            Server.getInstance().getClientMap().remove(key);
            key.cancel();
        }
    }

    /**
     * Handles the current packet.
     */
    private void handlePacket() {
        timeoutStopwatch.reset();
        int positionBefore = inData.position();
        StreamBuffer.ReadBuffer in = StreamBuffer.createReadBuffer(inData);

        // Handle the packet.
        try {
            switch (packetOpcode) {
                case 145: // Remove item.
                    int interfaceID = in.readShort(StreamBuffer.ValueType.A);
                    int slot = in.readShort(StreamBuffer.ValueType.A);
                    in.readShort(StreamBuffer.ValueType.A); // Item ID.

                    if (interfaceID == 1688) {
                        player.attributes.unequip(slot, player);
                    }
                    break;
                case 41: // Equip item.
                    in.readShort(); // Item ID.
                    slot = in.readShort(StreamBuffer.ValueType.A);
                    in.readShort(); // Interface ID.
                    player.attributes.equip(slot, player);
                    break;
                case 101: // Design character screen.
                    int gender = in.readByte();
                    int[] appearance = new int[7];
                    int[] colors = new int[5];

                    for (int i = 0; i < appearance.length; i++)
                        appearance[i] = in.readByte();

                    for (int i = 0; i < colors.length; i++)
                        colors[i] = in.readByte();

                    // Validate the interface is open
                    if (player.getCurrentInterfaceId() != 3559)
                        break;

                    // Validate values
                    if (gender != 0 && gender != 1)
                        break;

                    if (!Misc.validateColors(colors) || !Misc.validateAppearance(appearance))
                        break;

                    // Set changes
                    player.getAttributes().setGender(gender);
                    System.arraycopy(colors, 0, player.getAttributes().getColors(), 0, colors.length);
                    System.arraycopy(appearance, 0, player.getAttributes().getAppearance(), 0, appearance.length);

                    // Set update flags
                    player.getUpdateContext().setAppearanceUpdateRequired();
                    break;
                case 95: // Chat modes.
                    int publicChatMode = in.readByte(); // 0-3
                    int privateChatMode = in.readByte(); // 0-2
                    int tradeMode = in.readByte(); // 0-2

                    // Validate values
                    if (!Misc.in(publicChatMode, 0, 3) || !Misc.in(privateChatMode, 0, 2) || !Misc.in(tradeMode, 0, 2))
                        break;

                    // Ignore if no change was made
                    PlayerSettings settings = player.getAttributes().getSettings();

                    if (publicChatMode == settings.getPublicChatMode()
                            && privateChatMode == settings.getPrivateChatMode()
                            && tradeMode == settings.getTradeMode())
                        break;
                    PluginHandler.dispatchModifyChatMode(player, publicChatMode, privateChatMode, tradeMode);
                    settings.setPublicChatMode(publicChatMode);
                    settings.setPrivateChatMode(privateChatMode);
                    settings.setTradeMode(tradeMode);
                    break;
                case 185: // Button clicking.
                    PluginHandler.dispatchActionButton(player, StreamBuffer.hexToInt(in.readBytes(2)));
                    break;
                case 133: // Add ignore.
                    PluginHandler.dispatchAddIgnore(player, in.readLong());
                    break;
                case 74: // Remove ignore.
                    PluginHandler.dispatchRemoveIgnore(player, in.readLong());
                    break;
                case 188: // Add friend.
                    PluginHandler.dispatchAddFriend(player, in.readLong());
                    break;
                case 215: // Remove friend.
                    PluginHandler.dispatchRemoveFriend(player, in.readLong());
                    break;
                case 214: // Move item.
                    int frameId = in.readShort(StreamBuffer.ValueType.A, StreamBuffer.ByteOrder.LITTLE);
                    int insertMode = in.readByte();
                    int initialSlot = in.readShort(StreamBuffer.ValueType.A, StreamBuffer.ByteOrder.LITTLE);
                    int endSlot = in.readShort(StreamBuffer.ByteOrder.LITTLE);

                    // Inventory
                    if (frameId == 3214) {
                        player.getAttributes().swapInventoryItem(initialSlot, endSlot);
                    }
                    break;
                case 126: // Private message.
                    long username = in.readLong();
                    int chatLength = (packetLength - 8);
                    byte[] text = in.readBytes(chatLength);
                    PluginHandler.dispatchPrivateMessage(player, username, text);
                    break;
                case 4: // Player chat.
                    int effects = in.readByte(false, StreamBuffer.ValueType.S);
                    int color = in.readByte(false, StreamBuffer.ValueType.S);
                    chatLength = (packetLength - 2);
                    text = in.readBytesReverse(chatLength, StreamBuffer.ValueType.A);
                    PluginHandler.dispatchPublicMessage(player, new PublicChat(color, effects, text));
                    break;
                case 103: // Player command.
                    String command = in.readString();
                    String[] split = command.split(" ");
                    String[] args = Arrays.copyOfRange(split, 1, split.length);
                    PluginHandler.dispatchCommand(player, split[0].toLowerCase(), args);
                    break;
                case 248: // Movement.
                case 164: // ^
                case 98: // ^
                    int length = packetLength;

                    if (packetOpcode == 248) {
                        length -= 14;
                    }
                    int steps = (length - 5) / 2;
                    int[][] path = new int[steps][2];
                    int firstStepX = in.readShort(StreamBuffer.ValueType.A, StreamBuffer.ByteOrder.LITTLE);

                    for (int i = 0; i < steps; i++) {
                        path[i][0] = in.readByte();
                        path[i][1] = in.readByte();
                    }
                    int firstStepY = in.readShort(StreamBuffer.ByteOrder.LITTLE);
                    player.getMovementHandler().reset();
                    player.getMovementHandler().setRunPath(in.readByte(StreamBuffer.ValueType.C) == 1);
                    player.getMovementHandler().addToPath(new Position(firstStepX, firstStepY));

                    for (int i = 0; i < steps; i++) {
                        path[i][0] += firstStepX;
                        path[i][1] += firstStepY;
                        player.getMovementHandler().addToPath(new Position(path[i][0], path[i][1]));
                    }
                    player.getMovementHandler().finish();
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // Make sure we have finished reading all of this packet.
            int read = inData.position() - positionBefore;

            for (int i = read; i < packetLength; i++) {
                inData.get();
            }
        }
    }

    /**
     * Handles a received packet.
     */
    public final void handleIncomingData() {
        try {
            // Read the incoming data.
            if (getSocketChannel().read(inData) == -1) {
                disconnect();
                return;
            }

            // Handle the received data.
            inData.flip();

            while (inData.hasRemaining()) {
                // Handle login if we need to.
                if (getConnectionStage() != ConnectionStage.LOGGED_IN) {
                    handleLogin();
                    break;
                }

                // Decode the packet opcode.
                if (packetOpcode == -1) {
                    packetOpcode = inData.get() & 0xff;
                    packetOpcode = packetOpcode - getDecryptor().getNextValue() & 0xff;
                }

                // Decode the packet length.
                if (packetLength == -1) {
                    packetLength = PACKET_LENGTHS[packetOpcode];

                    if (packetLength == -1) {
                        if (!inData.hasRemaining()) {
                            inData.flip();
                            inData.compact();
                            break;
                        }
                        packetLength = inData.get() & 0xff;
                    }
                }

                // Decode the packet payload.
                if (inData.remaining() >= packetLength) {
                    handlePacket();

                    // Reset for the next packet.
                    packetOpcode = -1;
                    packetLength = -1;
                } else {
                    inData.flip();
                    inData.compact();
                    break;
                }
            }

            // Clear everything for the next read.
            inData.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
            disconnect();
        }
    }

    /**
     * Sends the buffer to the socket.
     *
     * @param buffer the buffer
     */
    public void send(ByteBuffer buffer) {
        // Prepare the buffer for writing.
        buffer.flip();

        try {
            // ...and write it!
            getSocketChannel().write(buffer);
        } catch (IOException ex) {
            disconnect();
        }
    }

    /**
     * Handles the login process of the client.
     */
    private void handleLogin() throws Exception {
        switch (getConnectionStage()) {
            case CONNECTED:
                if (inData.remaining() < 2) {
                    inData.compact();
                    return;
                }

                // Validate the request.
                int request = inData.get() & 0xff;
                inData.get(); // Name hash.

                if (request != 14) {
                    System.err.println("Invalid login request: " + request);
                    disconnect();
                    return;
                }

                // Write the response.
                StreamBuffer.WriteBuffer out = StreamBuffer.createWriteBuffer(17);
                out.writeLong(0); // First 8 bytes are ignored by the client.
                out.writeByte(0); // The response opcode, 0 for logging in.
                out.writeLong(new SecureRandom().nextLong()); // SSK.
                send(out.getBuffer());
                setConnectionStage(ConnectionStage.LOGGING_IN);
                break;
            case LOGGING_IN:
                if (inData.remaining() < 2) {
                    inData.compact();
                    return;
                }

                // Validate the login type.
                int loginType = inData.get();

                if (loginType != 16 && loginType != 18) {
                    System.err.println("Invalid login type: " + loginType);
                    disconnect();
                    return;
                }

                // Ensure that we can read all of the login block.
                int blockLength = inData.get() & 0xff;

                if (inData.remaining() < blockLength) {
                    inData.flip();
                    inData.compact();
                    return;
                }

                // Read the login block.
                StreamBuffer.ReadBuffer in = StreamBuffer.createReadBuffer(inData);
                in.readByte(); // Skip the magic ID value 255.

                // Validate the client version.
                int clientVersion = in.readShort();

                if (clientVersion != 317) {
                    System.err.println("Invalid client version: " + clientVersion);
                    disconnect();
                    return;
                }

                in.readByte(); // Skip the high/low memory version.

                // Skip the CRC keys.
                for (int i = 0; i < 9; i++) {
                    in.readInt();
                }

                in.readByte(); // Skip RSA block length.
                // If we wanted to, we would decode RSA at this point.

                // Validate that the RSA block was decoded properly.
                int rsaOpcode = in.readByte();

                if (rsaOpcode != 10) {
                    System.err.println("Unable to decode RSA block properly!");
                    disconnect();
                    return;
                }

                // Set up the ISAAC ciphers.
                long clientHalf = in.readLong();
                long serverHalf = in.readLong();
                int[] isaacSeed = {(int) (clientHalf >> 32), (int) clientHalf, (int) (serverHalf >> 32), (int) serverHalf};
                setDecryptor(new ISAACCipher(isaacSeed));

                for (int i = 0; i < isaacSeed.length; i++) {
                    isaacSeed[i] += 50;
                }
                setEncryptor(new ISAACCipher(isaacSeed));

                // Read the user authentication.
                in.readInt(); // Skip the user ID.
                String username = in.readString();
                String password = in.readString();

                // Attempting to log in
                login(username, password);
                setConnectionStage(ConnectionStage.LOGGED_IN);
                break;
        }
    }

    /**
     * Gets the remote host of the client.
     *
     * @return the host
     */
    public String getHost() {
        return getSocketChannel().socket().getInetAddress().getHostAddress();
    }

    /**
     * Gets the encryptor.
     */
    public ISAACCipher getEncryptor() {
        return encryptor;
    }

    /**
     * Sets the encryptor.
     */
    public void setEncryptor(ISAACCipher encryptor) {
        this.encryptor = encryptor;
    }

    /**
     * Gets the decryptor.
     */
    public ISAACCipher getDecryptor() {
        return decryptor;
    }

    /**
     * Sets the decryptor.
     */
    public void setDecryptor(ISAACCipher decryptor) {
        this.decryptor = decryptor;
    }

    /**
     * Gets the {@link Player} implementation of this superclass.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the {@link SocketChannel}.
     */
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    /**
     * Gets the {@link ConnectionStage}.
     */
    public ConnectionStage getConnectionStage() {
        return connectionStage;
    }

    /**
     * Sets the {@link ConnectionStage}.
     */
    public void setConnectionStage(ConnectionStage connectionStage) {
        this.connectionStage = connectionStage;
    }

    /**
     * Gets the {@link Misc.Stopwatch} used to time-out the client if they're inactive for too long.
     */
    public Misc.Stopwatch getTimeoutStopwatch() {
        return timeoutStopwatch;
    }

    /**
     * The current connection stage of the client.
     *
     * @author blakeman8192
     */
    protected enum ConnectionStage {
        CONNECTED, LOGGING_IN, LOGGED_IN, LOGGED_OUT
    }

}
