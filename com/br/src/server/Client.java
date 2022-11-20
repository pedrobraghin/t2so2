package com.br.src.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.net.Socket;
import java.net.SocketException;

import com.br.src.logger.Log;
import com.br.src.utils.DateTime;

import com.br.src.security.RSA;

public class Client implements Runnable {

    private boolean isConnected;

    private Socket client;
    private String ip;
    private int port;

    private PrintStream sendMessageToServer;
    private BufferedReader receiveMessageFromServer;
    private Stream stream;
    private RSA rsa;

    public Client(String host, int port, Stream stream) {
        this.stream = stream;
        this.ip = host;
        this.port = port;
        this.isConnected = false;
        this.rsa = new RSA();
    }

    @Override
    public void run() {
        String message;
        String descryptedMessage;
        try {
            isConnected = true;
            client = new Socket(ip, port);

            sendMessageToServer = new PrintStream(client.getOutputStream());
            receiveMessageFromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));

            while (isConnected && (message = receiveMessageFromServer.readLine()) != null) {
                descryptedMessage = rsa.decrypt(message);
                stream.println(descryptedMessage);
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
            sendMessageToServer.println(rsa.encrypt(message));
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
            if (sendMessageToServer != null) {
                sendMessageToServer.close();
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
