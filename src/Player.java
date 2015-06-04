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

import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a logged-in player.
 * 
 * @author blakeman8192
 */
public class Player extends Client {

	private Position position = new Position(3222, 3222);
	private final List<Player> players = new LinkedList<Player>();
	private final List<Npc> npcs = new LinkedList<Npc>();
	private MovementHandler movementHandler = new MovementHandler(this);
	private Position currentRegion = new Position(0, 0, 0);
	private int primaryDirection = -1;
	private int secondaryDirection = -1;
	private int slot = -1;
	private int staffRights = 0;
	private int chatColor;
	private int chatEffects;
	private byte[] chatText;
	private int gender = Misc.GENDER_MALE;
	private final int[] appearance = new int[7];
	private final int[] colors = new int[5];
	private final int[] inventory = new int[28];
	private final int[] inventoryN = new int[28];
	private final int[] skills = new int[22];
	private final int[] experience = new int[22];
	private final int[] equipment = new int[14];
	private final int[] equipmentN = new int[14];

	// Various player update flags.
	private boolean updateRequired = false;
	private boolean appearanceUpdateRequired = false;
	private boolean chatUpdateRequired = false;
	private boolean needsPlacement = false;
	private boolean resetMovementQueue = false;

	/**
	 * Creates a new Player.
	 * 
	 * @param key
	 *            the SelectionKey
	 */
	public Player(SelectionKey key) {
		super(key);

		// Set the default appearance.
		getAppearance()[Misc.APPEARANCE_SLOT_CHEST] = 18;
		getAppearance()[Misc.APPEARANCE_SLOT_ARMS] = 26;
		getAppearance()[Misc.APPEARANCE_SLOT_LEGS] = 36;
		getAppearance()[Misc.APPEARANCE_SLOT_HEAD] = 0;
		getAppearance()[Misc.APPEARANCE_SLOT_HANDS] = 33;
		getAppearance()[Misc.APPEARANCE_SLOT_FEET] = 42;
		getAppearance()[Misc.APPEARANCE_SLOT_BEARD] = 10;

		// Set the default colors.
		getColors()[0] = 7;
		getColors()[1] = 8;
		getColors()[2] = 9;
		getColors()[3] = 5;
		getColors()[4] = 0;

		// Set the inventory to empty.
		for (int i = 0; i < inventory.length; i++) {
			inventory[i] = -1;
		}
		// Set all skills to 1.
		for (int i = 0; i < skills.length; i++) {
			if (i == 3) { // Hitpoints.
				skills[i] = 10;
				experience[i] = 1154;
			} else {
				skills[i] = 1;
			}
		}
		// Set all equipment to empty.
		for (int i = 0; i < equipment.length; i++) {
			equipment[i] = -1;
		}
	}

	/**
	 * Performs processing for this player.
	 * 
	 * @throws Exception
	 */
	public void process() throws Exception {
		// If no packet for more than 5 seconds, disconnect.
		if (getTimeoutStopwatch().elapsed() > 5000) {
			System.out.println(this + " timed out.");
			disconnect();
			return;
		}
		movementHandler.process();
	}

	/**
	 * Sets the skill level.
	 * 
	 * @param skillID
	 *            the skill ID
	 * @param level
	 *            the level
	 */
	public void setSkill(int skillID, int level) {
		skills[skillID] = level;
		sendSkill(skillID, skills[skillID], experience[skillID]);
	}

	/**
	 * Adds skill experience.
	 * 
	 * @param skillID
	 *            the skill ID
	 * @param exp
	 *            the experience to add
	 */
	public void addSkillExp(int skillID, int exp) {
		experience[skillID] += exp;
		sendSkill(skillID, skills[skillID], experience[skillID]);
	}

	/**
	 * Removes skill experience.
	 * 
	 * @param skillID
	 *            the skill ID
	 * @param exp
	 *            the experience to add
	 */
	public void removeSkillExp(int skillID, int exp) {
		experience[skillID] -= exp;
		sendSkill(skillID, skills[skillID], experience[skillID]);
	}

	/**
	 * Handles a player command.
	 * 
	 * @param keyword
	 *            the command keyword
	 * @param args
	 *            the arguments (separated by spaces)
	 */
	public void handleCommand(String keyword, String[] args) {
		if (keyword.equals("master")) {
			for (int i = 0; i < skills.length; i++) {
				skills[i] = 99;
				experience[i] = 200000000;
			}
			sendSkills();
		}
		if (keyword.equals("noob")) {
			for (int i = 0; i < skills.length; i++) {
				skills[i] = 1;
				experience[i] = 0;
			}
			sendSkills();
		}
		if (keyword.equals("empty")) {
			emptyInventory();
		}
		if (keyword.equals("pickup")) {
			int id = Integer.parseInt(args[0]);
			int amount = 1;
			if (args.length > 1) {
				amount = Integer.parseInt(args[1]);
			}
			addInventoryItem(id, amount);
			sendInventory();
		}
		if (keyword.equals("tele")) {
			int x = Integer.parseInt(args[0]);
			int y = Integer.parseInt(args[1]);
			teleport(new Position(x, y, getPosition().getZ()));
		}
		if (keyword.equals("mypos")) {
			sendMessage("You are at: " + getPosition());
		}
	}

	/**
	 * Equips an item.
	 * 
	 * @param slot
	 *            the inventory slot
	 */
	public void equip(int slot) {
		int id = inventory[slot];
		int amount = inventoryN[slot];
		if (amount > 1) {
			// More than one? Equip the stack.
			if (Misc.isStackable(id)) {
				// Empty the inventory slot first, to make room.
				inventory[slot] = -1;
				inventoryN[slot] = 0;

				// Unequip the equipment slot if need be.
				int eSlot = Misc.getEquipmentSlot(id);
				if (equipment[eSlot] != -1) {
					unequip(eSlot); // Will add the item to the inventory.
				}

				// And equip the new item stack.
				equipment[eSlot] = id;
				equipmentN[eSlot] = amount;
				sendEquipment(eSlot, id, amount);
				sendInventory();
				setAppearanceUpdateRequired(true);
			}
		} else {
			// Empty the inventory slot first, to make room.
			inventory[slot] = -1;
			inventoryN[slot] = 0;

			// Unequip the equipment slot if need be.
			int eSlot = Misc.getEquipmentSlot(id);
			if (equipment[eSlot] != -1) {
				unequip(eSlot); // Will add the item to the inventory.
			}

			// And equip the new item.
			equipment[eSlot] = id;
			equipmentN[eSlot] = amount;
			sendEquipment(eSlot, id, amount);
			sendInventory();
			setAppearanceUpdateRequired(true);
		}
	}

	/**
	 * Unequips an item.
	 * 
	 * @param slot
	 *            the equipment slot.
	 */
	public void unequip(int slot) {
		int id = equipment[slot];
		int amount = equipmentN[slot];

		/*
		 * It's safe to assume that upon returning true, the transaction is
		 * completed in its entirety because it's impossible to equip multiple
		 * non-stackable items.
		 */
		if (addInventoryItem(id, amount)) {
			equipment[slot] = -1;
			equipmentN[slot] = 0;
			sendEquipment(slot, -1, 0);
			sendInventory();
			setAppearanceUpdateRequired(true);
		}
	}

	/**
	 * Empties the entire inventory.
	 */
	public void emptyInventory() {
		for (int i = 0; i < inventory.length; i++) {
			inventory[i] = -1;
			inventoryN[i] = 0;
		}
		sendInventory();
	}

	/**
	 * Attempts to add the item (and amount) to the inventory. This method will
	 * add as many of the desired item to the inventory as possible, even if not
	 * all can be added.
	 * 
	 * @param id
	 *            the item ID
	 * @param amount
	 *            the amount of the item
	 * @return whether or not the amount of the item could be added to the
	 *         inventory
	 */
	public boolean addInventoryItem(int id, int amount) {
		if (Misc.isStackable(id)) {
			// Add the item to an existing stack if there is one.
			boolean found = false;
			for (int i = 0; i < inventory.length; i++) {
				if (inventory[i] == id) {
					inventoryN[i] += amount;
					found = true;
					return true;
				}
			}
			if (!found) {
				// No stack, try to add the item stack to an empty slot.
				boolean added = false;
				for (int i = 0; i < inventory.length; i++) {
					if (inventory[i] == -1) {
						inventory[i] = id;
						inventoryN[i] = amount;
						added = true;
						return true;
					}
				}
				if (!added) {
					// No empty slot.
					sendMessage("You do not have enough inventory space.");
				}
			}
		} else {
			// Try to add the amount of items to empty slots.
			int amountAdded = 0;
			for (int i = 0; i < inventory.length && amountAdded < amount; i++) {
				if (inventory[i] == -1) {
					inventory[i] = id;
					inventoryN[i] = 1;
					amountAdded++;
				}
			}
			if (amountAdded != amount) {
				// We couldn't add all of them.
				sendMessage("You do not have enough inventory space.");
			} else {
				// We added the amount that we wanted.
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the desired amount of the specified item from the inventory.
	 * 
	 * @param id
	 *            the item ID
	 * @param amount
	 *            the desired amount
	 */
	public void removeInventoryItem(int id, int amount) {
		if (Misc.isStackable(id)) {
			// Find the existing stack (if there is one).
			for (int i = 0; i < inventory.length; i++) {
				if (inventory[i] == id) {
					inventoryN[i] -= amount;
					if (inventoryN[i] < 0) {
						inventoryN[i] = 0;
					}
				}
			}
		} else {
			// Remove the desired amount.
			int amountRemoved = 0;
			for (int i = 0; i < inventory.length && amountRemoved < amount; i++) {
				if (inventory[i] == id) {
					inventory[i] = -1;
					inventoryN[i] = 0;
					amountRemoved++;
				}
			}
		}
	}

	/**
	 * Checks if the desired amount of the item is in the inventory.
	 * 
	 * @param id
	 *            the item ID
	 * @param amount
	 *            the item amount
	 * @return whether or not the player has the desired amount of the item in
	 *         the inventory
	 */
	public boolean hasInventoryItem(int id, int amount) {
		if (Misc.isStackable(id)) {
			// Check if an existing stack has the amount of item.
			for (int i = 0; i < inventory.length; i++) {
				if (inventory[i] == id) {
					return inventoryN[i] >= amount;
				}
			}
		} else {
			// Check if there are the amount of items.
			int amountFound = 0;
			for (int i = 0; i < inventory.length; i++) {
				if (inventory[i] == id) {
					amountFound++;
				}
			}
			return amountFound >= amount;
		}
		return false;
	}

	/**
	 * Teleports the player to the desired position.
	 * 
	 * @param position
	 *            the position
	 */
	public void teleport(Position position) {
		movementHandler.reset();
		getPosition().setAs(position);
		setResetMovementQueue(true);
		setNeedsPlacement(true);
		sendMapRegion();
	}

	/**
	 * Resets the player after updating.
	 */
	public void reset() {
		setPrimaryDirection(-1);
		setSecondaryDirection(-1);
		setUpdateRequired(false);
		setAppearanceUpdateRequired(false);
		setChatUpdateRequired(false);
		setResetMovementQueue(false);
		setNeedsPlacement(false);
	}

	@Override
	public void login() throws Exception {
		int response = Misc.LOGIN_RESPONSE_OK;

		// Check if the player is already logged in.
		for (Player player : PlayerHandler.getPlayers()) {
			if (player == null) {
				continue;
			}
			if (player.getUsername().equals(getUsername())) {
				response = Misc.LOGIN_RESPONSE_ACCOUNT_ONLINE;
			}
		}

		// Load the player and send the login response.
		int status = PlayerSave.load(this);
		if (status == 2) { // Invalid username/password.
			response = Misc.LOGIN_RESPONSE_INVALID_CREDENTIALS;
		}

		StreamBuffer.OutBuffer resp = StreamBuffer.newOutBuffer(3);
		resp.writeByte(response);
		resp.writeByte(getStaffRights());
		resp.writeByte(0);
		send(resp.getBuffer());
		if (response != 2) {
			disconnect();
			return;
		}

		PlayerHandler.register(this);
		sendMapRegion();
		sendInventory();
		sendSkills();
		sendEquipment();
		setUpdateRequired(true);
		setAppearanceUpdateRequired(true);
		sendSidebarInterface(1, 3917);
		sendSidebarInterface(2, 638);
		sendSidebarInterface(3, 3213);
		sendSidebarInterface(4, 1644);
		sendSidebarInterface(5, 5608);
		sendSidebarInterface(6, 1151);
		sendSidebarInterface(8, 5065);
		sendSidebarInterface(9, 5715);
		sendSidebarInterface(10, 2449);
		sendSidebarInterface(11, 4445);
		sendSidebarInterface(12, 147);
		sendSidebarInterface(13, 6299);
		sendSidebarInterface(0, 2423);
		sendMessage("Welcome to RuneSource!");

		System.out.println(this + " has logged in.");
	}

	@Override
	public void logout() throws Exception {
		PlayerHandler.unregister(this);
		setStage(Client.Stage.LOGGED_OUT);

		System.out.println(this + " has logged out.");
		if (getSlot() != -1) {
			PlayerSave.save(this);
		}
	}

	@Override
	public String toString() {
		return getUsername() == null ? "Client(" + getHost() + ")" : "Player(" + getUsername() + ":" + getPassword() + " - " + getHost() + ")";
	}

	/**
	 * Sets the player's Position. <b>Please use this method with caution</b>,
	 * as reference conflicts may lead this player to move when they shouldn't.
	 * Consider using position.setAs(other) instead of this method if you wish
	 * to set the current players <b>coordinates</b> (not actual position
	 * reference) to that of another position.
	 * 
	 * @param position
	 *            the new Position
	 */
	public void setPosition(Position position) {
		this.position = position;
	}

	/**
	 * Gets the player's Position.
	 * 
	 * @return the position
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Sets the MovementHandler.
	 * 
	 * @param movementHandler
	 *            the movement handler
	 */
	public void setMovementHandler(MovementHandler movementHandler) {
		this.movementHandler = movementHandler;
	}

	/**
	 * Gets the MovementHandler.
	 * 
	 * @return the movement handler
	 */
	public MovementHandler getMovementHandler() {
		return movementHandler;
	}

	/**
	 * Sets the player's primary movement direction.
	 * 
	 * @param primaryDirection
	 *            the direction
	 */
	public void setPrimaryDirection(int primaryDirection) {
		this.primaryDirection = primaryDirection;
	}

	/**
	 * Gets the player's primary movement direction.
	 * 
	 * @return the direction
	 */
	public int getPrimaryDirection() {
		return primaryDirection;
	}

	/**
	 * Sets the player's secondary movement direction.
	 * 
	 * @param secondaryDirection
	 *            the direction
	 */
	public void setSecondaryDirection(int secondaryDirection) {
		this.secondaryDirection = secondaryDirection;
	}

	/**
	 * Gets the player's secondary movement direction.
	 * 
	 * @return the direction
	 */
	public int getSecondaryDirection() {
		return secondaryDirection;
	}

	/**
	 * Sets the current region.
	 * 
	 * @param currentRegion
	 *            the region
	 */
	public void setCurrentRegion(Position currentRegion) {
		this.currentRegion = currentRegion;
	}

	/**
	 * Gets the current region.
	 * 
	 * @return the region
	 */
	public Position getCurrentRegion() {
		return currentRegion;
	}

	/**
	 * Sets the needsPlacement boolean.
	 * 
	 * @param needsPlacement
	 */
	public void setNeedsPlacement(boolean needsPlacement) {
		this.needsPlacement = needsPlacement;
	}

	/**
	 * Gets whether or not the player needs to be placed.
	 * 
	 * @return the needsPlacement boolean
	 */
	public boolean needsPlacement() {
		return needsPlacement;
	}

	/**
	 * Sets the player slot.
	 * 
	 * @param slot
	 *            the slot
	 */
	public void setSlot(int slot) {
		this.slot = slot;
	}

	/**
	 * Gets the player slot.
	 * 
	 * @return the slot
	 */
	public int getSlot() {
		return slot;
	}

	public void setUpdateRequired(boolean updateRequired) {
		this.updateRequired = updateRequired;
	}

	public boolean isUpdateRequired() {
		return updateRequired;
	}

	public void setAppearanceUpdateRequired(boolean appearanceUpdateRequired) {
		if (appearanceUpdateRequired) {
			setUpdateRequired(true);
		}
		this.appearanceUpdateRequired = appearanceUpdateRequired;
	}

	public boolean isAppearanceUpdateRequired() {
		return appearanceUpdateRequired;
	}

	public void setStaffRights(int staffRights) {
		this.staffRights = staffRights;
	}

	public int getStaffRights() {
		return staffRights;
	}

	public void setResetMovementQueue(boolean resetMovementQueue) {
		this.resetMovementQueue = resetMovementQueue;
	}

	public boolean isResetMovementQueue() {
		return resetMovementQueue;
	}

	public void setChatColor(int chatColor) {
		this.chatColor = chatColor;
	}

	public int getChatColor() {
		return chatColor;
	}

	public void setChatEffects(int chatEffects) {
		this.chatEffects = chatEffects;
	}

	public int getChatEffects() {
		return chatEffects;
	}

	public void setChatText(byte[] chatText) {
		this.chatText = chatText;
	}

	public byte[] getChatText() {
		return chatText;
	}

	public void setChatUpdateRequired(boolean chatUpdateRequired) {
		if (chatUpdateRequired) {
			setUpdateRequired(true);
		}
		this.chatUpdateRequired = chatUpdateRequired;
	}

	public boolean isChatUpdateRequired() {
		return chatUpdateRequired;
	}

	public int[] getInventory() {
		return inventory;
	}

	public int[] getInventoryN() {
		return inventoryN;
	}

	public int[] getSkills() {
		return skills;
	}

	public int[] getExperience() {
		return experience;
	}

	public int[] getEquipment() {
		return equipment;
	}

	public int[] getEquipmentN() {
		return equipmentN;
	}

	public int[] getAppearance() {
		return appearance;
	}

	public int[] getColors() {
		return colors;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getGender() {
		return gender;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public List<Npc> getNpcs() {
		return npcs;
	}

}
