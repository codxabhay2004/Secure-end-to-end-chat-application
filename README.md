Secure Chat Application:

A secure end-to-end encrypted chat application developed using Java. The project focuses on providing private communication between users by implementing encryption, authentication, and replay protection mechanisms.

Overview:

This project was developed to demonstrate how secure communication can be achieved using a combination of networking and cryptographic techniques. The application follows a client-server architecture where the server only forwards encrypted messages and does not have access to the original message content.

The system establishes a secure session between users before communication begins. Once the session is created, all messages are encrypted before transmission and decrypted only at the receiver side.

Features:

-> End-to-end encrypted messaging
 
-> Secure key exchange between users

-> Message encryption and decryption

-> Authentication using digital signatures

-> Replay attack protection

-> Client-server communication

-> JavaFX based graphical interface

-> Modular project structure

Technologies Used
:

->Java

->JavaFX

->Maven

->Socket Programming

->Cryptography APIs

How the System Works:

->Users connect to the server using the client application.

->A secure key exchange process is performed between users.

->A shared session key is generated.

->Messages are encrypted before being sent.

->The server forwards encrypted messages.

->The receiver decrypts and displays the message.

How to Run:

Step 1:
Run: ChatServer.java

Step 2:
Run multiple instances of:
ChatClient.java

Step 3: Begin Secure Communication

-> Enter usernames

-> Start secure session

-> Send encrypted messages

Sample Features Demonstrated:

-> Secure communication between two users.

-> Encrypted message transmission

-> Authentication verification


Author

Abhay Singh

License

This project is developed for educational and learning purposes.
