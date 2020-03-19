package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ConnectionThread extends Thread {
    Socket connection;

    ConnectionThread(Socket connection) {
        this.connection = connection;
    }

    public void run() {
        System.out.println("Thread started with id " + this.getId());
        handleConnection(connection);
    }

    private static void handleConnection(Socket connection) {
        PrintWriter wr = null;
        try {
            // get method and path
            InputStream x = connection.getInputStream();
            wr = new PrintWriter(connection.getOutputStream());
            Scanner s = new Scanner(x);
            String line = s.nextLine();
            String[] components = line.split(" ");
            if (components.length != 3) {
                sendError(wr, "400", "Bad Request");
                return;
            }
            String method = components[0];
            String path = components[1];
            String protocol = components[2];
            if (!method.equals("GET") && !method.equals("POST")) {
                sendError(wr, "400", "Bad Request");
                return;
            } else if (!protocol.equals("HTTP/1.1") && !protocol.equals("HTTP/1.0")) {
                sendError(wr, "400", "Bad Request");
                return;
            }
            File requestedFile = new File(Main.ROOT_DIRECTORY + path);
            if (requestedFile.isDirectory()) {
                sendError(wr, "400", "Bad Request");
                return;
            }
            if (!requestedFile.exists()) {
                sendError(wr, "404", "Not Found");
                return;

            }
            if (!requestedFile.canWrite()) {
                sendError(wr, "403", "Forbidden");
                return;

            }
            System.out.println("OK, Sending file... " + path);
            // normal case here
            wr.write("HTTP/1.1 200 OK");
            wr.write("\r\n");
            wr.write("Connection: close");
            wr.write("\r\n");
            wr.write("Content-Type: " + getMimeType(path));
            wr.write("\r\n");
            wr.write("Content-Length: " + requestedFile.length());
            wr.write("\r\n");
            wr.write("\r\n");
            wr.flush();
            sendFile(requestedFile, connection.getOutputStream());
        } catch (Exception e) {
            if (wr != null)
                sendError(wr, "500", "Server Error");
        } finally {
            try {
                connection.close();
            } catch (IOException ignored) {
            }
            System.out.println("Connection closed");
        }
    }


    private static void sendError(PrintWriter wr, String error_code, String error_string) {
        System.out.println("sending error: " + error_code + " " + error_string);
        wr.write("HTTP/1.1 " + error_code + " " + error_string);
        wr.write("\r\n");
        wr.write("Connection: close");
        wr.write("Content-Type: text/html");
        wr.write("\r\n");
        wr.write("\r\n");
        wr.write(error_code + " " + error_string);
        wr.flush();
    }

    private static void sendFile(File file, OutputStream socketOut) throws
            IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        OutputStream out = new BufferedOutputStream(socketOut);
        while (true) {
            int x = in.read(); // read one byte from file
            if (x < 0)
                break; // end of file reached
            out.write(x);  // write the byte to the socket
        }
        out.flush();
    }

    private static String getMimeType(String fileName) {
        int pos = fileName.lastIndexOf('.');
        if (pos < 0)  // no file extension in name
            return "x-application/x-unknown";
        String ext = fileName.substring(pos + 1).toLowerCase();
        if (ext.equals("txt")) return "text/plain";
        else if (ext.equals("html")) return "text/html";
        else if (ext.equals("htm")) return "text/html";
        else if (ext.equals("css")) return "text/css";
        else if (ext.equals("js")) return "text/javascript";
        else if (ext.equals("java")) return "text/x-java";
        else if (ext.equals("jpeg")) return "image/jpeg";
        else if (ext.equals("jpg")) return "image/jpeg";
        else if (ext.equals("png")) return "image/png";
        else if (ext.equals("gif")) return "image/gif";
        else if (ext.equals("ico")) return "image/x-icon";
        else if (ext.equals("class")) return "application/java-vm";
        else if (ext.equals("jar")) return "application/java-archive";
        else if (ext.equals("zip")) return "application/zip";
        else if (ext.equals("xml")) return "application/xml";
        else if (ext.equals("xhtml")) return "application/xhtml+xml";
        else return "x-application/x-unknown";
        // Note:  x-application/x-unknown  is something made up;
        // it will probably make the browser offer to save the file.
    }
}
