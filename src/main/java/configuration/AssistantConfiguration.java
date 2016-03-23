package configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Josh on 23/03/2016.
 */
public class AssistantConfiguration extends FileConfiguration {
    private static final String ASSISTANT_FILE = "assistant.json";
    private static final JsonNode CONFIGURATION = FileConfiguration.getJson(ASSISTANT_FILE);
    private static PersonalConfiguration personal;
    private static Map<String, ModuleConfiguration> modules;

    public static PersonalConfiguration getPersonal() {
        if (personal == null) {
            personal = PersonalConfiguration.from(CONFIGURATION.get("personal"));
        }
        return personal;
    }

    public static ModuleConfiguration getModule(String module) {
        if (modules == null) {
            modules = new HashMap<>();

            for (JsonNode element : CONFIGURATION.get("modules")) {
                ModuleConfiguration moduleConfiguration = ModuleConfiguration.from(element);
                modules.put(moduleConfiguration.getName().toLowerCase(), moduleConfiguration);
            }
        }
        return modules.get(module.toLowerCase());
    }
}
