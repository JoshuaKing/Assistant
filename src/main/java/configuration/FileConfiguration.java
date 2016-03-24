package configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by Josh on 23/03/2016.
 */
abstract class FileConfiguration {
    private static final String CONFIG_DIR = "/configuration/";

    protected static JsonNode getJson(String filename) {
        try {
            return new ObjectMapper().readTree(FileConfiguration.class.getResourceAsStream(CONFIG_DIR + filename));
        } catch (IOException e) {
            e.printStackTrace();
            return new ObjectMapper().createObjectNode();
        }
    }
}
