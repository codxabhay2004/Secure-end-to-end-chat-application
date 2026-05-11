package crypto;

import java.security.*;

public class IdentityService {

    public static KeyPair generateIdentityKeyPair() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");

        return kpg.generateKeyPair();
    }

    public static byte[] sign(byte[] message, PrivateKey privateKey) throws Exception {

        Signature sig = Signature.getInstance("Ed25519");

        sig.initSign(privateKey);

        sig.update(message);

        return sig.sign();
    }

    public static boolean verify(byte[] message, byte[] signature, PublicKey publicKey) throws Exception {

        Signature sig = Signature.getInstance("Ed25519");

        sig.initVerify(publicKey);

        sig.update(message);

        return sig.verify(signature);
    }
}