package assistant;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import modules.ModuleConfig;
import modules.WestpacModule;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class Assistant {

    private static final String CONFIG_FILE = "configuration/assistant.json";

    private static final String CONFIG_ENCRYPTION = "configuration/encryption.json";

    public static void main(String[] args) throws FileNotFoundException {
        JsonObject assistantConfig = new Gson().fromJson(new FileReader(CONFIG_FILE), JsonObject.class);
        JsonObject encryptionConfig = new Gson().fromJson(new FileReader(CONFIG_ENCRYPTION), JsonObject.class);
        ModuleConfig mc = new ModuleConfig(assistantConfig);
        WestpacModule wm = new WestpacModule();
        wm.run(mc);
    }
}
