package configuration;

import com.google.gson.JsonObject;

/**
 * Created by Josh on 23/03/2016.
 */
public class EncryptionConfiguration extends FileConfiguration {
    private static final String ENCRYPTION_FILE = "encryption.json";
    private static final JsonObject CONFIGURATION = FileConfiguration.getJson(ENCRYPTION_FILE).getAsJsonObject();

    public static String getKeyBase64() {
        return CONFIGURATION.get("key").getAsString();
    }

    public static String getIvBase64() {
        return CONFIGURATION.get("iv").getAsString();
    }

    public static String getCipher() {
        return CONFIGURATION.get("cipher").getAsString();
    }

    public static String getMode() {
        return CONFIGURATION.get("mode").getAsString();
    }

    public static String getPadding() {
        return CONFIGURATION.get("padding").getAsString();
    }
}
