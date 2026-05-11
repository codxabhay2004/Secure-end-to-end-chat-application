package crypto;

import java.security.KeyPair;

public class IdentityTest {

    public static void main(String[] args) throws Exception {

        KeyPair kp = IdentityService.generateIdentityKeyPair();

        String message = "hello";

        byte[] sig = IdentityService.sign(
                message.getBytes(),
                kp.getPrivate()
        );

        boolean ok = IdentityService.verify(
                message.getBytes(),
                sig,
                kp.getPublic()
        );

        System.out.println("Signature valid: " + ok);
    }
}