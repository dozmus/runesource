package com.rs;

import com.cedarsoftware.util.io.JsonReader;
import com.rs.entity.Position;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * A collection of server settings, loaded from json.
 */
public class Settings {

    private String serverName;
    private int worldId;
    private Position startPosition;
    private int maxConsPerHost;
    private boolean hashPasswords;

    public static Settings load(String fileName) throws Exception {
        // Checking if file exists
        File file = new File(fileName);

        if (!file.exists()) {
            throw new FileNotFoundException("The settings file was not found");
        }

        // Reading file
        JsonReader reader = new JsonReader(new FileInputStream(file));
        Settings settings = (Settings) reader.readObject();
        reader.close();
        return settings;
    }

    public String getServerName() {
        return serverName;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public int getMaxConsPerHost() {
        return maxConsPerHost;
    }

    public boolean isHashingPasswords() {
        return hashPasswords;
    }

    public int getWorldId() {
        return worldId;
    }

}