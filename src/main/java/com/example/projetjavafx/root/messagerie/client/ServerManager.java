package com.example.projetjavafx.root.messagerie.client;


import com.example.projetjavafx.root.messagerie.server.ChatServer;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ServerManager {

    private static final int SERVER_PORT = 8887;
    private static boolean serverStarted = false;
    private static final int MAX_RETRY_ATTEMPTS = 10;
    private static final int RETRY_DELAY_MS = 1000;

    public static boolean isServerRunning() {
        try {
            Socket socket = new Socket("localhost", SERVER_PORT);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean ensureServerRunning() {
        if (isServerRunning()) {
            System.out.println("Server is already running");
            return true;
        }

        if (serverStarted) {
            System.out.println("Server startup was already attempted");
            return isServerRunning();
        }

        return startServer();
    }

    private static boolean startServer() {
        System.out.println("Starting chat server...");
        serverStarted = true;

        // Use a latch to wait for server startup
        final CountDownLatch serverStartLatch = new CountDownLatch(1);

        // Start server in a separate thread
        Thread serverThread = new Thread(() -> {
            try {
                System.out.println("Server thread starting...");
                ChatServer.startServer();
                serverStartLatch.countDown();
            } catch (Exception e) {
                System.err.println("Error in server thread: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Set as daemon so it doesn't prevent JVM shutdown
        serverThread.setDaemon(true);
        serverThread.start();

        try {
            // Wait for server to signal it's started or timeout after 5 seconds
            boolean started = serverStartLatch.await(5, TimeUnit.SECONDS);
            if (!started) {
                System.out.println("Server start signal timed out, checking if it's running anyway...");
            }

            // Verify server is actually running by attempting connection
            for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
                if (isServerRunning()) {
                    System.out.println("Server is now running (verified on attempt " + attempt + ")");
                    return true;
                }

                System.out.println("Server not responding yet, waiting... (attempt " + attempt + "/" + MAX_RETRY_ATTEMPTS + ")");
                Thread.sleep(RETRY_DELAY_MS);
            }

            System.err.println("Failed to start server after " + MAX_RETRY_ATTEMPTS + " attempts");
            return false;
        } catch (InterruptedException e) {
            System.err.println("Server startup wait was interrupted: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

