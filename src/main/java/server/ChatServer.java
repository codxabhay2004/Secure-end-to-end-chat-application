package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static final int PORT = 5000;

    // Store connected clients
    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            new ClientHandler(socket).start();
        }
    }

    static class ClientHandler extends Thread {

        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private String username;

        public ClientHandler(Socket socket) throws Exception {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        }

        public void run() {
            try {
                // First message is username
                username = in.readUTF();
                clients.put(username, this);
                System.out.println(username + " connected.");

                while (true) {

                    // Who the message is intended for
                    String targetUser = in.readUTF();

                    // Type of message (KEY_EXCHANGE or ENCRYPTED_MESSAGE)
                    String messageType = in.readUTF();

                    // Actual content (could be public key or encrypted text)
                    String payload = in.readUTF();

                    ClientHandler target = clients.get(targetUser);

                    if (target != null) {
                        target.out.writeUTF(username);
                        target.out.writeUTF(messageType);
                        target.out.writeUTF(payload);
                    }
                }

            } catch (Exception e) {
                System.out.println(username + " disconnected.");
                clients.remove(username);
            }
        }
    }
}