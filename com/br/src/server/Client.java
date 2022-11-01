package com.br.src.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

import com.br.src.logger.Log;
import com.br.src.utils.DateTime;

public class Client implements Runnable {

    private boolean isConnected;

    private Socket client;
    private String ip;
    private int port;

    private PrintStream sentMessageToServer;
    private BufferedReader receiveMessageFromServer;
    private Stream stream;

    public Client(String host, int port, Stream stream) {
        this.stream = stream;
        this.ip = host;
        this.port = port;
        this.isConnected = false;
    }

    @Override
    public void run() {
        String message;
        try {
            isConnected = true;
            client = new Socket(ip, port);
            sentMessageToServer = new PrintStream(client.getOutputStream());
            receiveMessageFromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while (isConnected && (message = receiveMessageFromServer.readLine()) != null) {
                stream.println(message);
            }
        } catch (SocketException e) {
            Log.saveLog("Error on Client Socket: " + e.getMessage());
            stream.println(formatMessage(
                    "Não foi possível estabelecer uma conexão com o host \"" + ip
                            + "\". Você pode iniciar um servidor local clicando no botão acima com o desenho de uma nuvem."));
        } catch (IOException e) {
            e.printStackTrace();
            stream.println(formatMessage("Chat-Server indisponível!"));
            Log.saveLog(e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private String formatMessage(String message) {
        String formatedMessage = DateTime.getDateTime() + message;
        return formatedMessage;
    }

    public void sendMessage(String message) {
        try {
            sentMessageToServer.println(message);
            if( message.startsWith("/exit")) {
                closeConnection();
            }
        } catch (NullPointerException e) {
            Log.saveLog(e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            isConnected = false;
            if (sentMessageToServer != null) {
                sentMessageToServer.close();
            }
            if (receiveMessageFromServer != null) {
                receiveMessageFromServer.close();
            }
            if (client != null && !client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            Log.saveLog(e.getMessage());
        } catch (Exception e) {
            Log.saveLog(e.getMessage());
        }
    }

    public boolean isConnected() {
        return this.isConnected;
    }
}
