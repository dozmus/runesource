package com.rs.io;
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
import com.rs.entity.player.PlayerAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.NoSuchFileException;

/**
 * An {@link PlayerFileHandler} implementation for JSON player attribute saving/loading.
 *
 * @author Pure_
 */
public final class JsonPlayerFileHandler implements PlayerFileHandler {

    @Override
    public void save(PlayerAttributes attributes) throws Exception {
        String fileName = getStorageDirectory() + attributes.getUsername() + ".json";
        JsonUtils.write(fileName, true, attributes);
    }

    @Override
    public PlayerAttributes load(String username) throws Exception {
        // Checking if file exists
        File file = new File(getStorageDirectory() + username + ".json");

        if (!file.exists()) {
            throw new NoSuchFileException(file.getAbsolutePath());
        }

        // Reading file
        JsonReader reader = new JsonReader(new FileInputStream(file));
        PlayerAttributes attributes = (PlayerAttributes) reader.readObject();
        reader.close();
        return attributes;
    }

    @Override
    public String getStorageDirectory() {
        return "./data/characters/";
    }
}
