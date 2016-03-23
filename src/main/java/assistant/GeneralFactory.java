package assistant;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import configuration.EncryptionConfiguration;
import encrypt.AesCipher;
import encrypt.Cipher;
import org.apache.http.conn.UnsupportedSchemeException;

/**
 * Created by Josh on 23/03/2016.
 */
public final class GeneralFactory {
    private static encrypt.Cipher cipher;

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
}
