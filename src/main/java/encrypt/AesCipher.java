package encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Created by Josh on 23/03/2016.
 */
public final class AesCipher implements encrypt.Cipher {
    private final byte[] key;
    private final byte[] iv;
    private final String spec;

    public AesCipher(String keyBase64, String ivBase64, String mode, String padding) {
        key = Base64.getDecoder().decode(keyBase64);
        iv = Base64.getDecoder().decode(ivBase64);
        spec = "AES/" + mode + "/" + padding;
    }

    @Override
    public String encrypt(String plaintext) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance(spec);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(plaintext.getBytes());
            System.out.println(plaintext + " encrypted string: " + Base64.getEncoder().encodeToString(encrypted));

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public String decrypt(String cipherText) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance(spec);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] plaintext = cipher.doFinal(Base64.getDecoder().decode(cipherText));

            return new String(plaintext);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
