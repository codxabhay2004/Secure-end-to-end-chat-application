package crypto;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.NamedParameterSpec;

public class KeyExchangeService {

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("X25519");
        kpg.initialize(new NamedParameterSpec("X25519"));
        return kpg.generateKeyPair();
    }

    public static byte[] deriveSharedSecret(PrivateKey privateKey, PublicKey publicKey) throws Exception {
        KeyAgreement ka = KeyAgreement.getInstance("X25519");
        ka.init(privateKey);
        ka.doPhase(publicKey, true);
        return ka.generateSecret();
    }
}