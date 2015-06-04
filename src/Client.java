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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * The class behind a Player that handles all networking-related things.
 * 
 * @author blakeman8192
 */
public abstract class Client {

	private final SelectionKey key;
	private final ByteBuffer inData;
	private final Player player = (Player) this;
	private final Misc.Stopwatch timeoutStopwatch = new Misc.Stopwatch();
	private SocketChannel socketChannel;

	private Stage stage;
	private int packetOpcode = -1;
	private int packetLength = -1;
	private String username;
	private String password;
	private ISAACCipher encryptor;
	private ISAACCipher decryptor;

	/**
	 * Creates a new Client.
	 * 
	 * @param key
	 *            the SelectionKey of the client
	 */
	public Client(SelectionKey key) {
		this.key = key;
		setStage(Stage.CONNECTED);
		inData = ByteBuffer.allocateDirect(512);
		if (key != null) {
			socketChannel = (SocketChannel) key.channel();
		}
	}

	/**
	 * Called after the player finishes logging in.
	 * 
	 * @throws Exception
	 */
	public abstract void login() throws Exception;

	/**
	 * Called before the player disconnects.
	 * 
	 * @throws Exception
	 */
	public abstract void logout() throws Exception;

	/**
	 * Sends all skills to the client.
	 */
	public void sendSkills() {
		for (int i = 0; i < player.getSkills().length; i++) {
			sendSkill(i, player.getSkills()[i], player.getExperience()[i]);
		}
	}

	/**
	 * Sends the skill to the client.
	 * 
	 * @param skillID
	 *            the skill ID
	 * @param level
	 *            the skill level
	 * @param exp
	 *            the skill experience
	 */
	public void sendSkill(int skillID, int level, int exp) {
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(8);
		out.writeHeader(getEncryptor(), 134);
		out.writeByte(skillID);
		out.writeInt(exp, StreamBuffer.ByteOrder.MIDDLE);
		out.writeByte(level);
		send(out.getBuffer());
	}

	/**
	 * Sends all equipment.
	 */
	public void sendEquipment() {
		for (int i = 0; i < player.getEquipment().length; i++) {
			sendEquipment(i, player.getEquipment()[i], player.getEquipmentN()[i]);
		}
	}

	/**
	 * Sends the equipment to the client.
	 * 
	 * @param slot
	 *            the equipment slot
	 * @param itemID
	 *            the item ID
	 * @param itemAmount
	 *            the item amount
	 */
	public void sendEquipment(int slot, int itemID, int itemAmount) {
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(32);
		out.writeVariableShortPacketHeader(getEncryptor(), 34);
		out.writeShort(1688);
		out.writeByte(slot);
		out.writeShort(itemID + 1);
		if (itemAmount > 254) {
			out.writeByte(255);
			out.writeShort(itemAmount);
		} else {
			out.writeByte(itemAmount);
		}
		out.finishVariableShortPacketHeader();
		send(out.getBuffer());
	}

	/**
	 * Sends the current full inventory.
	 */
	public void sendInventory() {
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(256);
		out.writeVariableShortPacketHeader(getEncryptor(), 53);
		out.writeShort(3214);
		out.writeShort(player.getInventory().length);
		for (int i = 0; i < player.getInventory().length; i++) {
			if (player.getInventoryN()[i] > 254) {
				out.writeByte(255);
				out.writeInt(player.getInventoryN()[i], StreamBuffer.ByteOrder.INVERSE_MIDDLE);
			} else {
				out.writeByte(player.getInventoryN()[i]);
			}
			out.writeShort(player.getInventory()[i] + 1, StreamBuffer.ValueType.A, StreamBuffer.ByteOrder.LITTLE);
		}
		out.finishVariableShortPacketHeader();
		send(out.getBuffer());
	}

	/**
	 * Sends a message to the players chat box.
	 * 
	 * @param message
	 *            the message
	 */
	public void sendMessage(String message) {
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(message.length() + 3);
		out.writeVariablePacketHeader(getEncryptor(), 253);
		out.writeString(message);
		out.finishVariablePacketHeader();
		send(out.getBuffer());
	}

	/**
	 * Sends a sidebar interface.
	 * 
	 * @param menuId
	 *            the interface slot
	 * @param form
	 *            the interface ID
	 */
	public void sendSidebarInterface(int menuId, int form) {
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(4);
		out.writeHeader(getEncryptor(), 71);
		out.writeShort(form);
		out.writeByte(menuId, StreamBuffer.ValueType.A);
		send(out.getBuffer());
	}

	/**
	 * Refreshes the map region.
	 */
	public void sendMapRegion() {
		player.getCurrentRegion().setAs(player.getPosition());
		player.setNeedsPlacement(true);
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(5);
		out.writeHeader(getEncryptor(), 73);
		out.writeShort(player.getPosition().getRegionX() + 6, StreamBuffer.ValueType.A);
		out.writeShort(player.getPosition().getRegionY() + 6);
		send(out.getBuffer());
	}

	/**
	 * Disconnects the client.
	 */
	public void disconnect() {
		System.out.println(this + " disconnecting.");
		try {
			logout();
			getSocketChannel().close();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			HostGateway.exit(getSocketChannel().socket().getInetAddress().getHostAddress());
			Server.getSingleton().getClientMap().remove(key);
			key.cancel();
		}
	}

	/**
	 * Handles a clicked button.
	 * 
	 * @param buttonId
	 *            the button ID
	 */
	private void handleButton(int buttonId) {
		switch (buttonId) {
		case 9154:
			sendLogout();
			break;
		case 153:
			player.getMovementHandler().setRunToggled(true);
			break;
		case 152:
			player.getMovementHandler().setRunToggled(false);
			break;
		default:
			System.out.println("Unhandled button: " + buttonId);
			break;
		}
	}

	/**
	 * Sends a packet that tells the client to log out.
	 */
	public void sendLogout() {
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(1);
		out.writeHeader(getEncryptor(), 109);
		send(out.getBuffer());
	}

	/**
	 * Handles the current packet.
	 */
	private void handlePacket() {
		timeoutStopwatch.reset();
		int positionBefore = inData.position();
		StreamBuffer.InBuffer in = StreamBuffer.newInBuffer(inData);

		// Handle the packet.
		try {
			switch (packetOpcode) {
			case 145: // Remove item.
				int interfaceID = in.readShort(StreamBuffer.ValueType.A);
				int slot = in.readShort(StreamBuffer.ValueType.A);
				in.readShort(StreamBuffer.ValueType.A); // Item ID.
				if (interfaceID == 1688) {
					player.unequip(slot);
				}
				break;
			case 41: // Equip item.
				in.readShort(); // Item ID.
				slot = in.readShort(StreamBuffer.ValueType.A);
				in.readShort(); // Interface ID.
				player.equip(slot);
				break;
			case 185: // Button clicking.
				handleButton(Misc.hexToInt(in.readBytes(2)));
				break;
			case 4: // Player chat.
				int effects = in.readByte(false, StreamBuffer.ValueType.S);
				int color = in.readByte(false, StreamBuffer.ValueType.S);
				int chatLength = (packetLength - 2);
				byte[] text = in.readBytesReverse(chatLength, StreamBuffer.ValueType.A);
				player.setChatEffects(effects);
				player.setChatColor(color);
				player.setChatText(text);
				player.setChatUpdateRequired(true);
				break;
			case 103: // Player command.
				String command = in.readString();
				String[] split = command.split(" ");
				player.handleCommand(split[0].toLowerCase(), Arrays.copyOfRange(split, 1, split.length));
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
				if (getStage() != Stage.LOGGED_IN) {
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
					packetLength = Misc.packetLengths[packetOpcode];
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
	 * @param buffer
	 *            the buffer
	 * @throws IOException
	 */
	public void send(ByteBuffer buffer) {
		// Prepare the buffer for writing.
		buffer.flip();

		try {
			// ...and write it!
			getSocketChannel().write(buffer);
		} catch (IOException ex) {
			ex.printStackTrace();
			disconnect();
		}
	}

	/**
	 * Handles the login process of the client.
	 */
	private void handleLogin() throws Exception {
		switch (getStage()) {
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
			StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(17);
			out.writeLong(0); // First 8 bytes are ignored by the client.
			out.writeByte(0); // The response opcode, 0 for logging in.
			out.writeLong(new SecureRandom().nextLong()); // SSK.
			send(out.getBuffer());

			setStage(Stage.LOGGING_IN);
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
			StreamBuffer.InBuffer in = StreamBuffer.newInBuffer(inData);
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
			int[] isaacSeed = { (int) (clientHalf >> 32), (int) clientHalf, (int) (serverHalf >> 32), (int) serverHalf };
			setDecryptor(new ISAACCipher(isaacSeed));
			for (int i = 0; i < isaacSeed.length; i++) {
				isaacSeed[i] += 50;
			}
			setEncryptor(new ISAACCipher(isaacSeed));

			// Read the user authentication.
			in.readInt(); // Skip the user ID.
			String username = in.readString();
			String password = in.readString();
			setUsername(username);
			setPassword(password);

			login();
			setStage(Stage.LOGGED_IN);
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
	 * Sets the username.
	 * 
	 * @param username
	 *            the username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the username.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the password.
	 * 
	 * @param password
	 *            the password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the encryptor.
	 * 
	 * @param encryptor
	 *            the encryptor
	 */
	public void setEncryptor(ISAACCipher encryptor) {
		this.encryptor = encryptor;
	}

	/**
	 * Gets the encryptor.
	 * 
	 * @return the encryptor
	 */
	public ISAACCipher getEncryptor() {
		return encryptor;
	}

	/**
	 * Sets the decryptor.
	 * 
	 * @param decryptor
	 *            the decryptor.
	 */
	public void setDecryptor(ISAACCipher decryptor) {
		this.decryptor = decryptor;
	}

	/**
	 * Gets the decryptor.
	 * 
	 * @return the decryptor
	 */
	public ISAACCipher getDecryptor() {
		return decryptor;
	}

	/**
	 * Gets the Player subclass implementation of this superclass.
	 * 
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the SocketChannel.
	 * 
	 * @return the SocketChannel
	 */
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public Stage getStage() {
		return stage;
	}

	public Misc.Stopwatch getTimeoutStopwatch() {
		return timeoutStopwatch;
	}

	/**
	 * The current connection stage of the client.
	 * 
	 * @author blakeman8192
	 */
	protected enum Stage {
		CONNECTED, LOGGING_IN, LOGGED_IN, LOGGED_OUT
	}

}
