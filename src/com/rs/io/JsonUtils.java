package com.rs.io;

import com.cedarsoftware.util.io.JsonWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {

    private static final Map<String, Object> WRITER_ARGS = new HashMap<>();

    static {
        WRITER_ARGS.put("JsonWriter.PRETTY_PRINT", true);
    }

    public static void write(String fileName, boolean overwrite, Object object) throws IOException {
        File file = new File(fileName);

        // Overwrite
        if (overwrite && file.exists()) {
            file.delete();
        }

        // Generating pretty json
        String json = JsonWriter.objectToJson(object, WRITER_ARGS);
        json = JsonWriter.formatJson(json);

        // Writing json
        FileWriter writer = new FileWriter(file);
        writer.write(json);
        writer.close();
    }
}
