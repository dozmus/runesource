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
import com.cedarsoftware.util.io.JsonWriter;
import com.rs.entity.player.Player;
import com.rs.entity.player.PlayerAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link PlayerFileHandler} implementation for JSON player attribute saving/loading.
 *
 * @author Pure_
 */
public final class JsonPlayerFileHandler implements PlayerFileHandler {

    @Override
    public void save(Player player) throws Exception {
        // Checking if file exists
        File file = new File(getStorageDirectory() + player.getAttributes().getUsername() + ".json");

        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
        }

        // Generating pretty json
        Map<String, Object> args = new HashMap<>();
        args.put("JsonWriter.PRETTY_PRINT", true);
        String json = JsonWriter.objectToJson(player.getAttributes(), args);
        json = JsonWriter.formatJson(json);

        // Writing json
        FileWriter writer = new FileWriter(file);
        writer.write(json);
        writer.close();
    }

    @Override
    public LoadResponse load(Player player) throws Exception {
        // Checking if file exists
        File file = new File(getStorageDirectory() + player.getAttributes().getUsername() + ".json");

        if (!file.exists()) {
            return LoadResponse.NOT_FOUND;
        }

        // Reading file
        JsonReader reader = new JsonReader(new FileInputStream(file));
        PlayerAttributes attributes = (PlayerAttributes) reader.readObject();
        reader.close();

        // Checking password
        if (!attributes.getPassword().equals(player.getAttributes().getPassword())) {
            return LoadResponse.INVALID_CREDENTIALS;
        }
        player.setAttributes(attributes);
        return LoadResponse.SUCCESS;
    }

    @Override
    public String getStorageDirectory() {
        return "./data/characters/";
    }
}
