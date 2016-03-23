package configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by Josh on 23/03/2016.
 */
public class ModuleConfiguration {
    private final String name;
    private final JsonObject config;

    private ModuleConfiguration(String name, JsonObject config) {
        this.name = name;
        this.config = config;
    }

    public static ModuleConfiguration from(JsonElement json) {
        String name = json.getAsJsonObject().get("module").getAsString();
        JsonObject config = json.getAsJsonObject().get("configuration").getAsJsonObject();
        return new ModuleConfiguration(name, config);
    }

    public JsonObject getJsonConfiguration() {
        return config;
    }

    public String getName() {
        return name;
    }
}
