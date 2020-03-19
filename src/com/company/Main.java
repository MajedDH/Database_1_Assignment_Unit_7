package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    private static final int LISTENING_PORT = 8090;
    public static String ROOT_DIRECTORY = "./files";

    public static void main(String[] args) {
        File f = new File(ROOT_DIRECTORY);
        if (!f.exists())
            f.mkdir();
        try {
            ROOT_DIRECTORY = f.getCanonicalPath();
        } catch (IOException ignored) {
        }

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(LISTENING_PORT);
        } catch (Exception e) {
            System.out.println("Failed to create listening socket.");
            return;
        }
        System.out.println("Listening on port " + LISTENING_PORT);
        try {
            while (true) {
                Socket connection = serverSocket.accept();
                System.out.println("\nConnection from "
                        + connection.getRemoteSocketAddress());
                ConnectionThread thread = new ConnectionThread(connection);
                thread.start();
            }
        } catch (Exception e) {
            System.out.println("Server socket shut down unexpectedly!");
            System.out.println("Error: " + e);
            System.out.println("Exiting.");
        }
    }


}
