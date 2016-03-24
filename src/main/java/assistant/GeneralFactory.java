package assistant;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import configuration.EncryptionConfiguration;
import encrypt.AesCipher;
import encrypt.Cipher;
import modules.AssistantModule;
import org.apache.http.conn.UnsupportedSchemeException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Josh on 23/03/2016.
 */
public final class GeneralFactory {
    private static encrypt.Cipher cipher;
    private static final Map<Class<? extends AssistantModule>, AwsFactory> awsFactories = new HashMap<>();

    public static Cipher getCipher() throws UnsupportedSchemeException {
        if (cipher == null) {
            if (EncryptionConfiguration.getCipher().equalsIgnoreCase("AES")) {
                cipher = new AesCipher(EncryptionConfiguration.getKeyBase64(), EncryptionConfiguration.getIvBase64(), EncryptionConfiguration.getMode(), EncryptionConfiguration.getPadding());
            } else {
                throw new UnsupportedSchemeException("Unsupported cipher type " + EncryptionConfiguration.getCipher());
            }
        }
        return cipher;
    }

    public static JBrowserDriver createDefaultBrowser() {
        return new JBrowserDriver();
    }

    public static AwsFactory getAwsFactory(Class<? extends AssistantModule> module) {
        if (!awsFactories.containsKey(module)) {
            awsFactories.put(module, new AwsFactory(module));
        }

        return awsFactories.get(module);
    }
}
