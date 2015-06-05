package com.rs;
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
 * @author Pure_
 */
public class Player extends Client {

    private final List<Player> players = new LinkedList<Player>();
    private final List<Npc> npcs = new LinkedList<Npc>();
    private PlayerAttributes attributes = new PlayerAttributes();
    private MovementHandler movementHandler = new MovementHandler(this);
    private Position currentRegion = new Position(0, 0, 0);
    private int primaryDirection = -1;
    private int secondaryDirection = -1;
    private int slot = -1;
    private int chatColor;
    private int chatEffects;
    private byte[] chatText;
    // Various player update flags.
    private boolean updateRequired = false;
    private boolean appearanceUpdateRequired = false;
    private boolean chatUpdateRequired = false;
    private boolean needsPlacement = false;
    private boolean resetMovementQueue = false;

    /**
     * Creates a new Player.
     *
     * @param key the SelectionKey
     */
    public Player(SelectionKey key) {
        super(key);

        // Resetting attributes
        attributes.reset();
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
     * @param skillID the skill ID
     * @param level   the level
     */
    public void setSkill(int skillID, int level) {
        attributes.getSkills()[skillID] = level;
        sendSkill(skillID, attributes.getSkills()[skillID], attributes.getExperience()[skillID]);
    }

    /**
     * Adds skill experience.
     *
     * @param skillID the skill ID
     * @param exp     the experience to add
     */
    public void addSkillExp(int skillID, int exp) {
        attributes.getExperience()[skillID] += exp;
        sendSkill(skillID, attributes.getSkills()[skillID], attributes.getExperience()[skillID]);
    }

    /**
     * Removes skill experience.
     *
     * @param skillID the skill ID
     * @param exp     the experience to add
     */
    public void removeSkillExp(int skillID, int exp) {
        attributes.getExperience()[skillID] -= exp;
        sendSkill(skillID, attributes.getSkills()[skillID], attributes.getExperience()[skillID]);
    }

    /**
     * Handles a player command.
     *
     * @param keyword the command keyword
     * @param args    the arguments (separated by spaces)
     */
    public void handleCommand(String keyword, String[] args) {
        if (keyword.equals("master")) {
            for (int i = 0; i < attributes.getSkills().length; i++) {
                attributes.getSkills()[i] = 99;
                attributes.getExperience()[i] = 200000000;
            }
            sendSkills();
        }
        if (keyword.equals("noob")) {
            for (int i = 0; i < attributes.getSkills().length; i++) {
                attributes.getSkills()[i] = 1;
                attributes.getExperience()[i] = 0;
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
     * @param slot the inventory slot
     */
    public void equip(int slot) {
        int id = attributes.getInventory()[slot];
        int amount = attributes.getInventoryN()[slot];
        if (amount > 1) {
            // More than one? Equip the stack.
            if (Misc.isStackable(id)) {
                // Empty the inventory slot first, to make room.
                attributes.getInventory()[slot] = -1;
                attributes.getInventoryN()[slot] = 0;

                // Unequip the equipment slot if need be.
                int eSlot = Misc.getEquipmentSlot(id);
                if (attributes.getEquipment()[eSlot] != -1) {
                    unequip(eSlot); // Will add the item to the inventory.
                }

                // And equip the new item stack.
                attributes.getEquipment()[eSlot] = id;
                attributes.getEquipmentN()[eSlot] = amount;
                sendEquipment(eSlot, id, amount);
                sendInventory();
                setAppearanceUpdateRequired(true);
            }
        } else {
            // Empty the inventory slot first, to make room.
            attributes.getInventory()[slot] = -1;
            attributes.getInventoryN()[slot] = 0;

            // Unequip the equipment slot if need be.
            int eSlot = Misc.getEquipmentSlot(id);
            if (attributes.getEquipment()[eSlot] != -1) {
                unequip(eSlot); // Will add the item to the inventory.
            }

            // And equip the new item.
            attributes.getEquipment()[eSlot] = id;
            attributes.getEquipmentN()[eSlot] = amount;
            sendEquipment(eSlot, id, amount);
            sendInventory();
            setAppearanceUpdateRequired(true);
        }
    }

    /**
     * Unequips an item.
     *
     * @param slot the equipment slot.
     */
    public void unequip(int slot) {
        int id = attributes.getEquipment()[slot];
        int amount = attributes.getEquipmentN()[slot];

		/*
         * It's safe to assume that upon returning true, the transaction is
		 * completed in its entirety because it's impossible to equip multiple
		 * non-stackable items.
		 */
        if (addInventoryItem(id, amount)) {
            attributes.getEquipment()[slot] = -1;
            attributes.getEquipmentN()[slot] = 0;
            sendEquipment(slot, -1, 0);
            sendInventory();
            setAppearanceUpdateRequired(true);
        }
    }

    /**
     * Empties the entire inventory.
     */
    public void emptyInventory() {
        for (int i = 0; i < attributes.getInventory().length; i++) {
            attributes.getInventory()[i] = -1;
            attributes.getInventoryN()[i] = 0;
        }
        sendInventory();
    }

    /**
     * Attempts to add the item (and amount) to the inventory. This method will
     * add as many of the desired item to the inventory as possible, even if not
     * all can be added.
     *
     * @param id     the item ID
     * @param amount the amount of the item
     * @return whether or not the amount of the item could be added to the
     * inventory
     */
    public boolean addInventoryItem(int id, int amount) {
        if (Misc.isStackable(id)) {
            // Add the item to an existing stack if there is one.
            boolean found = false;
            for (int i = 0; i < attributes.getInventory().length; i++) {
                if (attributes.getInventory()[i] == id) {
                    attributes.getInventoryN()[i] += amount;
                    found = true;
                    return true;
                }
            }
            if (!found) {
                // No stack, try to add the item stack to an empty slot.
                boolean added = false;
                for (int i = 0; i < attributes.getInventory().length; i++) {
                    if (attributes.getInventory()[i] == -1) {
                        attributes.getInventory()[i] = id;
                        attributes.getInventoryN()[i] = amount;
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
            for (int i = 0; i < attributes.getInventory().length && amountAdded < amount; i++) {
                if (attributes.getInventory()[i] == -1) {
                    attributes.getInventory()[i] = id;
                    attributes.getInventoryN()[i] = 1;
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
     * @param id     the item ID
     * @param amount the desired amount
     */
    public void removeInventoryItem(int id, int amount) {
        if (Misc.isStackable(id)) {
            // Find the existing stack (if there is one).
            for (int i = 0; i < attributes.getInventory().length; i++) {
                if (attributes.getInventory()[i] == id) {
                    attributes.getInventoryN()[i] -= amount;
                    if (attributes.getInventoryN()[i] < 0) {
                        attributes.getInventoryN()[i] = 0;
                    }
                }
            }
        } else {
            // Remove the desired amount.
            int amountRemoved = 0;
            for (int i = 0; i < attributes.getInventory().length && amountRemoved < amount; i++) {
                if (attributes.getInventory()[i] == id) {
                    attributes.getInventory()[i] = -1;
                    attributes.getInventoryN()[i] = 0;
                    amountRemoved++;
                }
            }
        }
    }

    /**
     * Checks if the desired amount of the item is in the inventory.
     *
     * @param id     the item ID
     * @param amount the item amount
     * @return whether or not the player has the desired amount of the item in
     * the inventory
     */
    public boolean hasInventoryItem(int id, int amount) {
        if (Misc.isStackable(id)) {
            // Check if an existing stack has the amount of item.
            for (int i = 0; i < attributes.getInventory().length; i++) {
                if (attributes.getInventory()[i] == id) {
                    return attributes.getInventoryN()[i] >= amount;
                }
            }
        } else {
            // Check if there are the amount of items.
            int amountFound = 0;
            for (int i = 0; i < attributes.getInventory().length; i++) {
                if (attributes.getInventory()[i] == id) {
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
     * @param position the position
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
    public void login(String username, String password) throws Exception {
        int response = Misc.LOGIN_RESPONSE_OK;

        // Updating credentials
        getAttributes().setUsername(username);
        getAttributes().setPassword(password);

        // Check if the player is already logged in.
        for (Player player : PlayerHandler.getPlayers()) {
            if (player == null) {
                continue;
            }
            if (player.getAttributes().getUsername().equals(getAttributes().getUsername())) {
                response = Misc.LOGIN_RESPONSE_ACCOUNT_ONLINE;
            }
        }

        // Load the player and send the login response.
        int status = PlayerAttributes.load(this);
        boolean validCredentials = Misc.validatePassword(getAttributes().getPassword()) && Misc.validateUsername(getAttributes().getUsername());

        // Invalid username/password - we skip the check if the account is found because the validation may have changed since
        if ((status != 0 && !validCredentials) || status == 2) {
            response = Misc.LOGIN_RESPONSE_INVALID_CREDENTIALS;
        }

        // Sending response
        StreamBuffer.OutBuffer resp = StreamBuffer.newOutBuffer(3);
        resp.writeByte(response);
        resp.writeByte(getAttributes().getStaffRights());
        resp.writeByte(0);
        send(resp.getBuffer());

        if (response != 2) {
            disconnect();
            return;
        }

        // Initialising player session
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
            PlayerAttributes.save(this);
        }
    }

    @Override
    public String toString() {
        return getAttributes().getUsername() == null ? "Client(" + getHost() + ")" : "Player(" + getAttributes().getUsername() + "@" + getHost() + ")";
    }

    /**
     * Gets the player's Position.
     *
     * @return the position
     */
    public Position getPosition() {
        return getAttributes().getPosition();
    }

    /**
     * Sets the player's Position. <b>Please use this method with caution</b>,
     * as reference conflicts may lead this player to move when they shouldn't.
     * Consider using position.setAs(other) instead of this method if you wish
     * to set the current players <b>coordinates</b> (not actual position
     * reference) to that of another position.
     *
     * @param position the new Position
     */
    public void setPosition(Position position) {
        getAttributes().setPosition(position);
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
     * Sets the MovementHandler.
     *
     * @param movementHandler the movement handler
     */
    public void setMovementHandler(MovementHandler movementHandler) {
        this.movementHandler = movementHandler;
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
     * Sets the player's primary movement direction.
     *
     * @param primaryDirection the direction
     */
    public void setPrimaryDirection(int primaryDirection) {
        this.primaryDirection = primaryDirection;
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
     * Sets the player's secondary movement direction.
     *
     * @param secondaryDirection the direction
     */
    public void setSecondaryDirection(int secondaryDirection) {
        this.secondaryDirection = secondaryDirection;
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
     * Sets the current region.
     *
     * @param currentRegion the region
     */
    public void setCurrentRegion(Position currentRegion) {
        this.currentRegion = currentRegion;
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
     * Gets the player slot.
     *
     * @return the slot
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Sets the player slot.
     *
     * @param slot the slot
     */
    public void setSlot(int slot) {
        this.slot = slot;
    }

    public boolean isUpdateRequired() {
        return updateRequired;
    }

    public void setUpdateRequired(boolean updateRequired) {
        this.updateRequired = updateRequired;
    }

    public boolean isAppearanceUpdateRequired() {
        return appearanceUpdateRequired;
    }

    public void setAppearanceUpdateRequired(boolean appearanceUpdateRequired) {
        if (appearanceUpdateRequired) {
            setUpdateRequired(true);
        }
        this.appearanceUpdateRequired = appearanceUpdateRequired;
    }

    public boolean isResetMovementQueue() {
        return resetMovementQueue;
    }

    public void setResetMovementQueue(boolean resetMovementQueue) {
        this.resetMovementQueue = resetMovementQueue;
    }

    public int getChatColor() {
        return chatColor;
    }

    public void setChatColor(int chatColor) {
        this.chatColor = chatColor;
    }

    public int getChatEffects() {
        return chatEffects;
    }

    public void setChatEffects(int chatEffects) {
        this.chatEffects = chatEffects;
    }

    public byte[] getChatText() {
        return chatText;
    }

    public void setChatText(byte[] chatText) {
        this.chatText = chatText;
    }

    public boolean isChatUpdateRequired() {
        return chatUpdateRequired;
    }

    public void setChatUpdateRequired(boolean chatUpdateRequired) {
        if (chatUpdateRequired) {
            setUpdateRequired(true);
        }
        this.chatUpdateRequired = chatUpdateRequired;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Npc> getNpcs() {
        return npcs;
    }

    public PlayerAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(PlayerAttributes attributes) {
        this.attributes = attributes;
    }

}
