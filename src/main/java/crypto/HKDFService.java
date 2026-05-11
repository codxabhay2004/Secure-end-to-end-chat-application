package crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public class HKDFService {

    public static byte[] deriveKey(byte[] sharedSecret, byte[] salt, String info) throws Exception {

        Mac mac = Mac.getInstance("HmacSHA256");

        // Step 1: Extract
        SecretKeySpec saltKey = new SecretKeySpec(salt, "HmacSHA256");
        mac.init(saltKey);
        byte[] prk = mac.doFinal(sharedSecret);

        // Step 2: Expand
        mac.init(new SecretKeySpec(prk, "HmacSHA256"));
        mac.update(info.getBytes());
        mac.update((byte) 1);
        byte[] okm = mac.doFinal();

        return Arrays.copyOf(okm, 32); // 256-bit key
    }

    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}