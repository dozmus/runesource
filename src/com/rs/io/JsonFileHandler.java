package com.rs.io;

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
 * An implementation of json player saving/loading.
 *
 * @author Pure_
 */
public class JsonFileHandler implements PlayerFileHandler {

    @Override
    public void save(Player player) throws Exception {
        // Checking if file exists
        File file = new File(getDirectory() + player.getAttributes().getUsername() + ".json");

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

    @Override
    public LoadResponse load(Player player) throws Exception {
        // Checking if file exists
        File file = new File(getDirectory() + player.getAttributes().getUsername() + ".json");

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
    public String getDirectory() {
        return "./data/characters/";
    }
}
