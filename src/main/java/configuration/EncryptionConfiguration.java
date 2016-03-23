package configuration;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Josh on 23/03/2016.
 */
public class EncryptionConfiguration extends FileConfiguration {
    private static final String ENCRYPTION_FILE = "encryption.json";
    private static final JsonNode CONFIGURATION = FileConfiguration.getJson(ENCRYPTION_FILE);

    public static String getKeyBase64() {
        return CONFIGURATION.get("key").asText();
    }

    public static String getIvBase64() {
        return CONFIGURATION.get("iv").asText();
    }

    public static String getCipher() {
        return CONFIGURATION.get("cipher").asText();
    }

    public static String getMode() {
        return CONFIGURATION.get("mode").asText();
    }

    public static String getPadding() {
        return CONFIGURATION.get("padding").asText();
    }
}
