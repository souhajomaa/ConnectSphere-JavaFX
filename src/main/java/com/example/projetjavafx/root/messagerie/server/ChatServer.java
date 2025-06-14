package com.example.projetjavafx.root.messagerie.server;

import com.example.projetjavafx.root.messagerie.model.Message;
import com.example.projetjavafx.root.messagerie.util.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


public class ChatServer extends WebSocketServer {

    private static final int PORT = 8887;
    private Map<Integer, WebSocket> userConnections = new HashMap<>();
    private Gson gson;
    private static ChatServer instance;

    public ChatServer() {
        super(new InetSocketAddress(PORT));
        setReuseAddr(true);

        // Créer une instance de Gson configurée avec l'adaptateur pour LocalDateTime
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .serializeNulls()
                .create();

        // Add connection timeout settings
        setConnectionLostTimeout(100);
    }

    public static synchronized ChatServer getInstance() {
        if (instance == null) {
            instance = new ChatServer();
        }
        return instance;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress());
        // Remove user from connections map
        userConnections.entrySet().removeIf(entry -> entry.getValue() == conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message on server: " + message);

        try {
            Message msg = gson.fromJson(message, Message.class);

            switch (msg.getType()) {
                case "REGISTER":
                    // Register user connection
                    userConnections.put(msg.getSenderId(), conn);
                    System.out.println("Registered user: " + msg.getSenderId() + " with connection: " + conn.getRemoteSocketAddress());

                    // Send confirmation back to client
                    Message confirmMsg = new Message("SYSTEM", 0, msg.getSenderId(),
                            "Connected to server successfully. Your user ID: " + msg.getSenderId());
                    conn.send(gson.toJson(confirmMsg));
                    break;

                case "MESSAGE":
                    // Forward message to recipient
                    WebSocket recipientConn = userConnections.get(msg.getRecipientId());
                    if (recipientConn != null && recipientConn.isOpen()) {
                        System.out.println("Forwarding message to user: " + msg.getRecipientId() + " at " + recipientConn.getRemoteSocketAddress());
                        recipientConn.send(message);
                        System.out.println("Message forwarded successfully");
                    } else {
                        System.out.println("Recipient not connected or connection not open. User ID: " + msg.getRecipientId());
                        // Store message for offline delivery or notify sender
                        conn.send(gson.toJson(new Message("SYSTEM", 0, msg.getSenderId(),
                                "User is offline. Message will be delivered when they connect.")));
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();

            // Send error message back to client
            try {
                conn.send(gson.toJson(new Message("SYSTEM", 0, 0,
                        "Error processing your message: " + e.getMessage())));
            } catch (Exception ex) {
                System.err.println("Error sending error message: " + ex.getMessage());
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error with connection: " + (conn != null ? conn.getRemoteSocketAddress() : "null"));
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Chat server started on port: " + PORT);
    }

    public static void main(String[] args) {
        startServer();
    }

    public static void startServer() {
        try {
            // Check if server is already running
            try (Socket socket = new Socket("localhost", PORT)) {
                System.out.println("Server is already running on port " + PORT);
                return;
            } catch (IOException e) {
                // Port is available, continue with server startup
                System.out.println("Port " + PORT + " is available, starting server...");
            }

            ChatServer server = getInstance();

            // Add shutdown hook to stop server gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                try {
                    server.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            // Start server with explicit try-catch
            try {
                server.start();
                System.out.println("ChatServer started successfully on port: " + PORT);
            } catch (Exception e) {
                System.err.println("Failed to start server: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Critical error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}