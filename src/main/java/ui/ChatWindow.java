package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ChatWindow extends Application {

    TextArea chatArea;
    TextField messageField;

    private client.ChatClient client;
    private String username;
    private String targetUser;

    @Override
    public void start(Stage stage) {

        chatArea = new TextArea();
        chatArea.setEditable(false);

        messageField = new TextField();
        messageField.setPromptText("Type message...");

        Button sendButton = new Button("Send");

        HBox bottomBox = new HBox(10, messageField, sendButton);

        BorderPane root = new BorderPane();
        root.setCenter(chatArea);
        root.setBottom(bottomBox);

        username = "Alice";
        targetUser = "Bob";

        try {
            client = new client.ChatClient();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(root, 500, 400);

        stage.setTitle("Secure Chat");
        stage.setScene(scene);
        stage.show();

        sendButton.setOnAction(e -> sendMessage());
    }

    private void sendMessage() {

        String message = messageField.getText();

        if (!message.isEmpty()) {

            chatArea.appendText("Me: " + message + "\n");

            System.out.println("Sending message to " + targetUser + ": " + message);

            messageField.clear();
        }
    }

    public void receiveMessage(String sender, String message) {
        chatArea.appendText(sender + ": " + message + "\n");
    }

    public static void main(String[] args) {
        launch();
    }
}