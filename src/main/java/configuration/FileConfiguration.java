package configuration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by Josh on 23/03/2016.
 */
abstract class FileConfiguration {
    private static final String CONFIG_DIR = "configuration/";

    protected static JsonElement getJson(String filename) {
        try {
            return new Gson().fromJson(new JsonReader(new FileReader(CONFIG_DIR + filename)), JsonElement.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new JsonObject();
        }
    }
}
