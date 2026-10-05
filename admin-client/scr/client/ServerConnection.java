package com.phongmay.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() throws IOException {
        if (isConnected()) {
            return true;
        }

        socket = new Socket(host, port);
        socket.setSoTimeout(15000);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        return isConnected();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public com.phongmay.cilent.Response sendRequest(com.phongmay.cilent.Request request) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (!isConnected()) {
            throw new IllegalStateException("Server connection is not established");
        }

        writer.println(request.toJson());
        String responseJson = reader.readLine();
        if (responseJson == null || responseJson.isBlank()) {
            return null;
        }

        return com.phongmay.cilent.Response.fromJson(responseJson);
    }

    public void sendRaw(String json) {
        if (!isConnected()) {
            throw new IllegalStateException("Server connection is not established");
        }
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("JSON payload cannot be empty");
        }
        writer.println(json);
    }

    public String receiveRaw() throws IOException {
        if (!isConnected()) {
            throw new IllegalStateException("Server connection is not established");
        }
        String line = reader.readLine();
        return line == null || line.isBlank() ? null : line;
    }

    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
        if (writer != null) {
            writer.close();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
