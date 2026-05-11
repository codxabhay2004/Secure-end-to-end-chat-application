package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import crypto.KeyExchangeService;
import crypto.HKDFService;
import crypto.AESGCMService;
import crypto.IdentityService;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.security.KeyFactory;
import java.util.Base64;

public class ChatClient {

    public static void main(String[] args) throws Exception {

        final byte[][] saltHolder = new byte[1][];
        final byte[][] sessionKeyHolder = new byte[1][];
        final int[] sendCounter = new int[1];
        final int[] receiveCounter = new int[1];
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        Socket socket = new Socket("localhost", 5000);

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // Send username
        out.writeUTF(username);

        // Generate keypair
        KeyPair myKeyPair = KeyExchangeService.generateKeyPair();
        KeyPair identityKeyPair = crypto.IdentityService.generateIdentityKeyPair();
        // ================= RECEIVE THREAD =================
        new Thread(() -> {
            try {
                while (true) {

                    String sender = in.readUTF();
                    String messageType = in.readUTF();
                    String payload = in.readUTF();

                    if (messageType.equals("KEY_EXCHANGE")) {

                        System.out.println("Received key exchange from " + sender);

                        String[] parts = payload.split(":");

                        String publicKeyBase64 = parts[0];

                        String saltBase64 = null;
                        String signatureBase64 = null;
                        String identityPublicKeyBase64 = null;

                        if (parts.length == 4) {
                            // Initiator message
                            saltBase64 = parts[1];
                            signatureBase64 = parts[2];
                            identityPublicKeyBase64 = parts[3];
                        } else if (parts.length == 3) {
                            // Responder message
                            signatureBase64 = parts[1];
                            identityPublicKeyBase64 = parts[2];
                        }
                        PublicKey senderIdentityKey = null;



                        if (identityPublicKeyBase64 != null) {

                            byte[] identityKeyBytes =
                                    Base64.getDecoder().decode(identityPublicKeyBase64);

                            KeyFactory idFactory = KeyFactory.getInstance("Ed25519");

                            senderIdentityKey =
                                    idFactory.generatePublic(
                                            new X509EncodedKeySpec(identityKeyBytes));
                        }

                        byte[] signatureBytes =
                                Base64.getDecoder().decode(signatureBase64);

                        byte[] receivedKeyBytes =
                                Base64.getDecoder().decode(publicKeyBase64);

                        KeyFactory kf = KeyFactory.getInstance("X25519");
                        PublicKey senderPublicKey =
                                kf.generatePublic(
                                        new X509EncodedKeySpec(receivedKeyBytes));

                        if (signatureBase64 != null && senderIdentityKey != null) {

                            signatureBytes =
                                    Base64.getDecoder().decode(signatureBase64);

                            boolean valid =
                                    IdentityService.verify(
                                            senderPublicKey.getEncoded(),
                                            signatureBytes,
                                            senderIdentityKey);

                            if (!valid) {
                                System.out.println("❌ Signature verification failed!");
                                continue;
                            }

                            System.out.println("Signature verified.");
                        }



                        byte[] sharedSecret =
                                KeyExchangeService.deriveSharedSecret(
                                        myKeyPair.getPrivate(),
                                        senderPublicKey);

                        byte[] salt;

                        if (saltBase64 != null) {
                            salt = Base64.getDecoder().decode(saltBase64);
                            saltHolder[0] = salt;
                            System.out.println("Using received salt.");
                        } else {
                            salt = saltHolder[0];
                            System.out.println("Reusing stored salt.");
                        }

                        sessionKeyHolder[0] =
                                HKDFService.deriveKey(
                                        sharedSecret,
                                        salt,
                                        "chat-session");

                        System.out.println("Secure session established with " + sender);

                        // If sender included salt, we are responder
                        if (saltBase64 != null) {

                            String myPublicKeyBase64 =
                                    Base64.getEncoder()
                                            .encodeToString(
                                                    myKeyPair.getPublic().getEncoded());

// sign our X25519 public key
                            signatureBytes = IdentityService.sign(
                                    myKeyPair.getPublic().getEncoded(),
                                    identityKeyPair.getPrivate());

                            signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);

// send identity public key
                            identityPublicKeyBase64 = Base64.getEncoder()
                                    .encodeToString(
                                            identityKeyPair.getPublic().getEncoded());

                            String responsePayload =
                                    myPublicKeyBase64 + ":" + signatureBase64 + ":" + identityPublicKeyBase64;

                            out.writeUTF(sender);
                            out.writeUTF("KEY_EXCHANGE");
                            out.writeUTF(responsePayload);

                            System.out.println("Sent public key back to " + sender);


                        }

                        System.out.println("Session key length: "
                                + sessionKeyHolder[0].length);
                        System.out.println("----------------------------------");

                    } else if (messageType.equals("ENCRYPTED")) {

                        if (sessionKeyHolder[0] == null) {
                            System.out.println("No session key available.");
                            continue;
                        }

                        byte[] combined =
                                Base64.getDecoder().decode(payload);

                        byte[] nonce = new byte[12];
                        System.arraycopy(combined, 0, nonce, 0, 12);

                        byte[] ciphertext =
                                new byte[combined.length - 12];

                        System.arraycopy(combined, 12,
                                ciphertext, 0, ciphertext.length);

                        byte[] decrypted =
                                AESGCMService.decrypt(
                                        sessionKeyHolder[0],
                                        nonce,
                                        ciphertext);

                        String decryptedText = new String(decrypted);

                        // split counter and message
                        String[] parts = decryptedText.split(":", 2);

                        int msgCounter = Integer.parseInt(parts[0]);
                        String actualMessage = parts[1];
                        // replay protection check
                        if (msgCounter <= receiveCounter[0]) {
                            System.out.println("⚠ Replay attack detected. Message ignored.");
                            continue;
                        }

// update counter
                        receiveCounter[0] = msgCounter;

                        System.out.println(sender + ": " + actualMessage);
                        System.out.println("----------------------------------");

                    } else {

                        System.out.println("Received from " + sender);
                        System.out.println("Type: " + messageType);
                        System.out.println("Payload: " + payload);
                        System.out.println("----------------------------------");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // ================= SEND LOOP =================
        while (true) {

            System.out.print("Send to: ");
            String target = scanner.nextLine();

            System.out.print("Type (1=Start Secure Chat, 2=Send Message): ");
            String option = scanner.nextLine();

            if (option.equals("1")) {

                // Generate random salt
                byte[] salt = HKDFService.generateSalt();

                // Store salt (important!)
                saltHolder[0] = salt;

                String saltBase64 =
                        Base64.getEncoder().encodeToString(salt);

                String publicKeyBase64 =
                        Base64.getEncoder()
                                .encodeToString(
                                        myKeyPair.getPublic().getEncoded());
                // Sign the public key
                byte[] signatureBytes =
                        IdentityService.sign(
                                myKeyPair.getPublic().getEncoded(),
                                identityKeyPair.getPrivate());

                String signatureBase64 =
                        Base64.getEncoder().encodeToString(signatureBytes);
                String identityPublicKeyBase64 =
                        Base64.getEncoder()
                                .encodeToString(
                                        identityKeyPair.getPublic().getEncoded());

                String combinedPayload =
                        publicKeyBase64 + ":" + saltBase64 + ":" + signatureBase64 + ":" + identityPublicKeyBase64;
                out.writeUTF(target);
                out.writeUTF("KEY_EXCHANGE");
                out.writeUTF(combinedPayload);

                System.out.println("Public key + salt sent to " + target);

            } else if (option.equals("2")) {

                System.out.print("Message: ");
                String message = scanner.nextLine();

                // increase message counter
                sendCounter[0]++;

                // attach counter to message
                String counterMessage = sendCounter[0] + ":" + message;

                if (sessionKeyHolder[0] == null) {
                    System.out.println("Secure session not established yet!");
                    continue;
                }

                AESGCMService.EncryptedData encrypted =
                        AESGCMService.encrypt(
                                sessionKeyHolder[0],
                                counterMessage.getBytes());

                byte[] combined =
                        new byte[encrypted.nonce.length
                                + encrypted.ciphertext.length];

                System.arraycopy(encrypted.nonce, 0,
                        combined, 0, encrypted.nonce.length);

                System.arraycopy(encrypted.ciphertext, 0,
                        combined, encrypted.nonce.length,
                        encrypted.ciphertext.length);

                String encoded =
                        Base64.getEncoder().encodeToString(combined);

                out.writeUTF(target);
                out.writeUTF("ENCRYPTED");
                out.writeUTF(encoded);

                System.out.println("Encrypted message sent.");

            } else {
                System.out.println("Invalid option.");
            }
        }
    }
}