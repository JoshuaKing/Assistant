package encrypt;

/**
 * Created by Josh on 23/03/2016.
 */
public interface Cipher {
    String encrypt(String plaintext);
    String decrypt(String cipherText);
}
