package modules;

import com.google.gson.JsonElement;

/**
 * Created by Josh on 1/03/2016.
 */
public class ModuleConfig {

    JsonElement config;

    public ModuleConfig(JsonElement config) {
        this.config = config;
    }

    public JsonElement getConfig() {
        return config;
    }
}
