package configuration;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Josh on 23/03/2016.
 */
public class ModuleConfiguration {
    private final String name;
    private final JsonNode config;

    private ModuleConfiguration(String name, JsonNode config) {
        this.name = name;
        this.config = config;
    }

    public static ModuleConfiguration from(JsonNode json) {
        String name = json.get("module").asText();
        JsonNode config = json.get("configuration");
        return new ModuleConfiguration(name, config);
    }

    public JsonNode getJsonConfiguration() {
        return config;
    }

    public String getName() {
        return name;
    }
}
