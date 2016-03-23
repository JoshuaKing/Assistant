package modules;

import configuration.AssistantConfiguration;
import configuration.ModuleConfiguration;
import log.LogLevel;
import log.Logger;

/**
 * Created by Josh on 1/03/2016.
 */
public abstract class AssistantModule {
    protected final ModuleConfiguration configuration;
    protected final String moduleName;
    protected final Logger logger;

    protected <T extends AssistantModule> AssistantModule(Class<T> module) {
        moduleName = module.getSimpleName();
        configuration = AssistantConfiguration.getModule(moduleName);
        logger = new Logger(getLogLevel());
    }

    private LogLevel getLogLevel() {
        try {
            LogLevel level = LogLevel.valueOf(configuration.getJsonConfiguration()
                    .get("log")
                    .getAsString()
                    .trim()
                    .toUpperCase()
            );

            if (level == null) return LogLevel.DEBUG;
            return level;
        } catch (Exception e) {
            return LogLevel.DEBUG;
        }
    }

    public static void moduleRunner(AssistantModule... modules) {
        for (AssistantModule module : modules) {
            module.run();
        }
    }

    public abstract void run();
}
