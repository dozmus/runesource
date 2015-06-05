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
import com.rs.entity.Position;
import com.rs.util.Misc;

/**
 * Represents all player attributes that are saved to the disk.
 *
 * @author Pure_
 */
public class PlayerAttributes {

    private String username;
    private String password;
    private Position position = new Position(0, 0);
    private int staffRights = 0;
    private int gender = Misc.GENDER_MALE;
    private final int[] appearance = new int[7];
    private final int[] colors = new int[5];
    private final int[] skills = new int[22];
    private final int[] experience = new int[22];
    private final int[] inventory = new int[28];
    private final int[] inventoryN = new int[28];
    private final int[] equipment = new int[14];
    private final int[] equipmentN = new int[14];

    public int[] getColors() {
        return colors;
    }

    public int[] getAppearance() {
        return appearance;
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

    public void reset() {
        // Setting the default position
        position.setAs(Server.getInstance().getSettings().getStartPosition());

        // Set the default appearance.
        appearance[Misc.APPEARANCE_SLOT_CHEST] = 18;
        appearance[Misc.APPEARANCE_SLOT_ARMS] = 26;
        appearance[Misc.APPEARANCE_SLOT_LEGS] = 36;
        appearance[Misc.APPEARANCE_SLOT_HEAD] = 0;
        appearance[Misc.APPEARANCE_SLOT_HANDS] = 33;
        appearance[Misc.APPEARANCE_SLOT_FEET] = 42;
        appearance[Misc.APPEARANCE_SLOT_BEARD] = 10;

        // Set the default colors.
        colors[0] = 7;
        colors[1] = 8;
        colors[2] = 9;
        colors[3] = 5;
        colors[4] = 0;

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

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getStaffRights() {
        return staffRights;
    }

    public void setStaffRights(int staffRights) {
        this.staffRights = staffRights;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the skill level.
     *
     * @param skillId the skill ID
     * @param level   the level
     * @param player
     */
    public void setSkill(int skillId, int level, Player player) {
        getSkills()[skillId] = level;
        player.sendSkill(skillId, getSkills()[skillId], getExperience()[skillId]);
    }

    /**
     * Adds skill experience.
     *
     * @param skillid the skill ID
     * @param exp     the experience to add
     * @param player
     */
    public void addSkillExp(int skillid, int exp, Player player) {
        getExperience()[skillid] += exp;
        player.sendSkill(skillid, getSkills()[skillid], getExperience()[skillid]);
    }

    /**
     * Removes skill experience.
     *
     * @param skillid the skill ID
     * @param exp     the experience to add
     * @param player
     */
    public void removeSkillExp(int skillid, int exp, Player player) {
        getExperience()[skillid] -= exp;
        player.sendSkill(skillid, getSkills()[skillid], getExperience()[skillid]);
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
            for (int i = 0; i < getInventory().length; i++) {
                if (getInventory()[i] == id) {
                    return getInventoryN()[i] >= amount;
                }
            }
        } else {
            // Check if there are the amount of items.
            int amountFound = 0;
            for (int i = 0; i < getInventory().length; i++) {
                if (getInventory()[i] == id) {
                    amountFound++;
                }
            }
            return amountFound >= amount;
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
            for (int i = 0; i < getInventory().length; i++) {
                if (getInventory()[i] == id) {
                    getInventoryN()[i] -= amount;
                    if (getInventoryN()[i] < 0) {
                        getInventoryN()[i] = 0;
                    }
                }
            }
        } else {
            // Remove the desired amount.
            int amountRemoved = 0;
            for (int i = 0; i < getInventory().length && amountRemoved < amount; i++) {
                if (getInventory()[i] == id) {
                    getInventory()[i] = -1;
                    getInventoryN()[i] = 0;
                    amountRemoved++;
                }
            }
        }
    }

    /**
     * Attempts to add the item (and amount) to the inventory. This method will
     * add as many of the desired item to the inventory as possible, even if not
     * all can be added.
     *
     * @param id     the item ID
     * @param amount the amount of the item
     * @param player
     * @return whether or not the amount of the item could be added to the
     * inventory
     */
    public boolean addInventoryItem(int id, int amount, Player player) {
        if (Misc.isStackable(id)) {
            // Add the item to an existing stack if there is one.
            boolean found = false;
            for (int i = 0; i < getInventory().length; i++) {
                if (getInventory()[i] == id) {
                    getInventoryN()[i] += amount;
                    found = true;
                    return true;
                }
            }
            if (!found) {
                // No stack, try to add the item stack to an empty slot.
                boolean added = false;
                for (int i = 0; i < getInventory().length; i++) {
                    if (getInventory()[i] == -1) {
                        getInventory()[i] = id;
                        getInventoryN()[i] = amount;
                        added = true;
                        return true;
                    }
                }
                if (!added) {
                    // No empty slot.
                    player.sendMessage("You do not have enough inventory space.");
                }
            }
        } else {
            // Try to add the amount of items to empty slots.
            int amountAdded = 0;
            for (int i = 0; i < getInventory().length && amountAdded < amount; i++) {
                if (getInventory()[i] == -1) {
                    getInventory()[i] = id;
                    getInventoryN()[i] = 1;
                    amountAdded++;
                }
            }
            if (amountAdded != amount) {
                // We couldn't add all of them.
                player.sendMessage("You do not have enough inventory space.");
            } else {
                // We added the amount that we wanted.
                return true;
            }
        }
        return false;
    }

    /**
     * Unequips an item.
     *
     * @param slot   the equipment slot.
     * @param player
     */
    public void unequip(int slot, Player player) {
        int id = getEquipment()[slot];
        int amount = getEquipmentN()[slot];

		/*
         * It's safe to assume that upon returning true, the transaction is
		 * completed in its entirety because it's impossible to equip multiple
		 * non-stackable items.
		 */
        if (addInventoryItem(id, amount, player)) {
            getEquipment()[slot] = -1;
            getEquipmentN()[slot] = 0;
            player.sendEquipment(slot, -1, 0);
            player.sendInventory();
            player.setAppearanceUpdateRequired(true);
        }
    }

    /**
     * Equips an item.
     *
     * @param slot   the inventory slot
     * @param player
     */
    public void equip(int slot, Player player) {
        int id = getInventory()[slot];
        int amount = getInventoryN()[slot];
        if (amount > 1) {
            // More than one? Equip the stack.
            if (Misc.isStackable(id)) {
                // Empty the inventory slot first, to make room.
                getInventory()[slot] = -1;
                getInventoryN()[slot] = 0;

                // Unequip the equipment slot if need be.
                int eSlot = Misc.getEquipmentSlot(id);
                if (getEquipment()[eSlot] != -1) {
                    unequip(eSlot, player); // Will add the item to the inventory.
                }

                // And equip the new item stack.
                getEquipment()[eSlot] = id;
                getEquipmentN()[eSlot] = amount;
                player.sendEquipment(eSlot, id, amount);
                player.sendInventory();
                player.setAppearanceUpdateRequired(true);
            }
        } else {
            // Empty the inventory slot first, to make room.
            getInventory()[slot] = -1;
            getInventoryN()[slot] = 0;

            // Unequip the equipment slot if need be.
            int eSlot = Misc.getEquipmentSlot(id);
            if (getEquipment()[eSlot] != -1) {
                unequip(eSlot, player); // Will add the item to the inventory.
            }

            // And equip the new item.
            getEquipment()[eSlot] = id;
            getEquipmentN()[eSlot] = amount;
            player.sendEquipment(eSlot, id, amount);
            player.sendInventory();
            player.setAppearanceUpdateRequired(true);
        }
    }

    /**
     * Empties the entire inventory.
     *
     * @param player
     */
    public void emptyInventory(Player player) {
        for (int i = 0; i < getInventory().length; i++) {
            getInventory()[i] = -1;
            getInventoryN()[i] = 0;
        }
        player.sendInventory();
    }
}
