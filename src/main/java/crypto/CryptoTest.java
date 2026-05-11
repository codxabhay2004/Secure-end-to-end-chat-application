package crypto;

import java.security.KeyPair;
import java.util.Arrays;

public class CryptoTest {

    public static void main(String[] args) throws Exception {

        // 1️⃣ Key exchange
        KeyPair alice = KeyExchangeService.generateKeyPair();
        KeyPair bob = KeyExchangeService.generateKeyPair();

        byte[] secretA = KeyExchangeService.deriveSharedSecret(
                alice.getPrivate(), bob.getPublic());

        byte[] secretB = KeyExchangeService.deriveSharedSecret(
                bob.getPrivate(), alice.getPublic());

        System.out.println("Secrets match: " + Arrays.equals(secretA, secretB));

        // 2️⃣ HKDF
        byte[] salt = HKDFService.generateSalt();
        byte[] aesKeyA = HKDFService.deriveKey(secretA, salt, "chat-session");
        byte[] aesKeyB = HKDFService.deriveKey(secretB, salt, "chat-session");

        System.out.println("AES keys match: " + Arrays.equals(aesKeyA, aesKeyB));

        // 3️⃣ Encrypt
        String message = "Hello Secure World!";
        AESGCMService.EncryptedData encrypted =
                AESGCMService.encrypt(aesKeyA, message.getBytes());

        // 4️⃣ Decrypt
        byte[] decrypted = AESGCMService.decrypt(
                aesKeyB,
                encrypted.nonce,
                encrypted.ciphertext);

        System.out.println("Decrypted message: " + new String(decrypted));
    }
}