package crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class AESGCMService {

    private static final int GCM_TAG_LENGTH = 128;

    public static class EncryptedData {
        public byte[] nonce;
        public byte[] ciphertext;

        public EncryptedData(byte[] nonce, byte[] ciphertext) {
            this.nonce = nonce;
            this.ciphertext = ciphertext;
        }
    }

    public static EncryptedData encrypt(byte[] key, byte[] plaintext) throws Exception {

        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] ciphertext = cipher.doFinal(plaintext);

        return new EncryptedData(nonce, ciphertext);
    }

    public static byte[] decrypt(byte[] key, byte[] nonce, byte[] ciphertext) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        return cipher.doFinal(ciphertext);
    }
}