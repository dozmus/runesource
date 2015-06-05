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

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents all player attributes that are saved to the disk.
 *
 * @author Pure_
 */
public class PlayerAttributes {

    /**
     * The directory where players are saved.
     */
    public static final String directory = "./data/characters/";
    private String username;
    private String password;
    private Position position = new Position(3222, 3222);
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

    /**
     * Saves a player's attributes.
     *
     * @param player the player to save
     */
    public static void save(Player player) throws Exception {
        // Checking if file exists
        File file = new File(directory + player.getAttributes().getUsername() + ".json");

        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
        }

        // Generating pretty json
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("JsonWriter.PRETTY_PRINT", true);
        String json = JsonWriter.objectToJson(player.getAttributes(), args);
        json = JsonWriter.formatJson(json);

        // Writing json
        FileWriter writer = new FileWriter(file);
        writer.write(json);
        writer.close();
    }

    /**
     * Loads a player's attributes.
     *
     * @param player the player to load.
     * @return 0 for success, 1 if the player does not have a saved game, 2 for
     * invalid username/password
     */
    public static int load(Player player) throws Exception {
        // Checking if file exists
        File file = new File(directory + player.getAttributes().getUsername() + ".json");

        if (!file.exists()) {
            return 1;
        }

        // Reading file
        JsonReader reader = new JsonReader(new FileInputStream(file));
        PlayerAttributes attributes = (PlayerAttributes) reader.readObject();
        reader.close();

        // Checking password
        if (!attributes.getPassword().equals(player.getAttributes().getPassword())) {
            return 2;
        }
        player.setAttributes(attributes);
        return 0;
    }

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
}
