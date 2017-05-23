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
import com.rs.entity.Position;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * A collection of server settings, loaded from json.
 */
public final class Settings {

    private String serverName;
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

}