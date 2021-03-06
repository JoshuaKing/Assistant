package modules;

import com.fasterxml.jackson.databind.JsonNode;
import configuration.AssistantConfiguration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

public abstract class AssistantModule {
    protected final JsonNode configuration;
    protected final String moduleName;
    static final Logger LOGGER = LoggerFactory.getLogger(AssistantModule.class);

    protected <T extends AssistantModule> AssistantModule(Class<T> module) {
        moduleName = module.getSimpleName();
        configuration = AssistantConfiguration.getModule(moduleName).getJsonConfiguration();
    }

    public static void moduleRunner(AssistantModule... modules) {
        for (AssistantModule module : modules) {
            module.run();
        }
    }

    protected String getResource(String filename) {
        StringWriter stringWriter = new StringWriter();
        try {
            IOUtils.copy(getClass().getResourceAsStream("/" + moduleName + "/" + filename), stringWriter, "UTF-8");
        } catch (IOException e) {
            LOGGER.error("Error Loading resource");
            LOGGER.error(e.getMessage());
        }
        return stringWriter.toString();
    }

    protected String getResourcePath() {
        return getResourcePath("");
    }

    protected String getResourcePath(String filepath) {
        return getClass().getResource("/" + moduleName + "/" + filepath).getPath();
    }

    public abstract void run();
}
