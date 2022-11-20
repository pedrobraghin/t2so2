package com.br.src.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.UUID;

import com.br.src.logger.Log;
import com.br.src.security.RSA;
import com.br.src.utils.DateTime;

public class Server implements Runnable {

    private final int PORT = 4567;
    private final RSA rsa;
    private ServerSocket server;
    private boolean isServerRunning;
    private ArrayList<ConnectionHandler> allConn;
    private Thread connectionsThread;

    public Server() {
        rsa = new RSA();
        isServerRunning = false;
        allConn = new ArrayList<ConnectionHandler>();
        connectionsThread = new Thread(new ConnectionManager());
    }

    @Override
    public void run() {
        isServerRunning = true;
        try {
            server = new ServerSocket(PORT);
            connectionsThread.start();
            synchronized (server) {
                try {
                    server.wait();
                } catch (InterruptedException e) {
                    Log.saveLog("Error while waiting for server close: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.saveLog(e.getMessage());
        } finally {
            System.out.println("Servidor encerrado.");
            closeServer();
        }
    }

    public boolean isServerRunning() {
        return this.isServerRunning;
    }

    public void closeServer() {
        isServerRunning = false;
        try {
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler conn : allConn) {
                conn.closeConnection();
            }
        } catch (IOException e) {
            Log.saveLog(e.getMessage());
        } finally {
            synchronized (server) {
                server.notifyAll();
            }
        }
    }

    private class ConnectionManager implements Runnable {
        private ExecutorService executor;

        @Override
        public void run() {
            try {
                executor = Executors.newCachedThreadPool();
                while (isServerRunning) {
                    Socket client = server.accept();
                    ConnectionHandler conn = new ConnectionHandler(client);
                    executor.execute(conn);
                    allConn.add(conn);
                }
            } catch (Exception e) {
                Log.saveLog(e.getMessage());
            } finally {
                closeServer();
            }
        }
    }

    private class ConnectionHandler implements Runnable {
        private Socket client;
        private String nickname;
        private String clientIP;
        private PrintStream sendMessageToClient;
        private BufferedReader receiveMessageFromClient;
        private final String ID = UUID.randomUUID().toString();

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            String clientMessage;
            clientIP = client.getInetAddress().getHostAddress();
            nickname = clientIP;
            try {
                sendMessageToClient = new PrintStream(client.getOutputStream(), true);
                receiveMessageFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
                sendMessageToClient("[Servidor] Conexão estabelecida.");

                System.out.println("Usuário " + ID + " se conectou.");
                loginClient();
                if (client.isClosed()) {
                    return;
                }
                broadcast("[Servidor] " + nickname + " se conectou.", ID);

                
                while ((clientMessage = readMessage(receiveMessageFromClient.readLine())) != null) {
                    if (clientMessage.startsWith("/private")) {
                        String[] privateMessage = clientMessage.split(" ", 3);
                        boolean isMessageSent = false;
                        if (privateMessage.length != 3) {
                            sendMessageToClient("[Servidor] Para enviar uma mensagem privada siga o seguinte formato: /private <nickname do receptor> <mensagem>.");
                        } else {
                            if (privateMessage[1].equals(nickname)) {
                                sendMessageToClient("[Servidor] Não é possível enviar uma mensagem para você mesmo.");
                            } else {
                                isMessageSent = sendPrivateMessage(privateMessage[2], nickname, privateMessage[1]);
                                if (isMessageSent) {
                                    sendMessageToClient("[Servidor] Mensagem enviada para " + privateMessage[1]);
                                } else {
                                    sendMessageToClient("[Servidor] Não foi possível enviar mensagem para " + privateMessage[1] + ". Usuário não encontrado!");
                                }
                            }
                        }
                    } else if (clientMessage.startsWith("/exit")) {
                        broadcast("[Servidor] " + nickname + " se desconectou.", ID);
                        System.out.println("Usuário " + ID + " se desconectou.");
                        closeConnection();
                    } else {
                        broadcast(formatMessageWithNickname(clientMessage, nickname), ID);
                    }
                }
            } catch (SocketException e) {
                if (nickname.startsWith("/exit")) {
                    nickname = "user";
                }
                Log.saveLog(nickname + "\\" + clientIP + " leave the server: " + e.getMessage());
            } catch (IOException e) {
                Log.saveLog("Error on Server Socket: " + e.getMessage());
            } finally {
                System.out.println("Usuário " + ID + " se desconectou.");
                closeConnection();
            }
        }

        private void sendMessageToClient(String message) {
            sendMessageToClient.println(rsa.encrypt(formatMessage(message)));
        }

        private void sendPrivateMessageToClient(String message, String recipient) {
            sendMessageToClient.println(rsa.encrypt(formatPrivateMessage(message, recipient)));
        }

        private void loginClient() {
            boolean isValidNickname = true;
            try {
                sendMessageToClient("[Servidor] Informe um nickname para se conectar ao chat: ");

                nickname = readMessage(receiveMessageFromClient.readLine());
                isValidNickname = validateNickname(nickname, ID);
                if (nickname.startsWith("/exit")) {
                    broadcast("[Servidor] " + nickname + " se desconectou.", ID);
                    closeConnection();
                    return;
                }
                while (!isValidNickname) {
                    sendMessageToClient("[Servidor] Este nickname já está em uso, escolha outra: ");

                    nickname = readMessage(receiveMessageFromClient.readLine());
                    isValidNickname = validateNickname(nickname, ID);
                }
                sendMessageToClient("[Servidor] Seja bem-vindo(a) ao Chat " + nickname);
            } catch (IOException e) {
                Log.saveLog(e.getMessage());
            } catch (NullPointerException e) {
            }
        }

        public String readMessage(String message) {
            return rsa.decrypt(message);
        }

        public String getNickname() {
            return this.nickname;
        }

        public String getId() {
            return this.ID;
        }

        private void broadcast(String message, String id) {
            for (ConnectionHandler conn : allConn) {
                if (conn != null && (!conn.getId().equals(id))) {
                    conn.sendMessageToClient(message);
                }
            }
        }

        private boolean sendPrivateMessage(String message, String sender, String recipient) {
            boolean isMessageSent = false;
            for (ConnectionHandler conn : allConn) {
                if (conn != null) {
                    if (conn.getNickname().equals(recipient)) {
                        conn.sendPrivateMessageToClient(message, recipient);
                        isMessageSent = true;
                        break;
                    }
                }
            }
            return isMessageSent;
        }

        private String formatMessage(String message) {
            String formatedMessage = DateTime.getDateTime() + message;
            return formatedMessage;
        }

        private String formatMessageWithNickname(String message, String nickname) {
            String formatedMessage = "";
            formatedMessage = "[" + nickname + "]: " + message;
            return formatedMessage;
        }

        private String formatPrivateMessage(String message, String nickname) {
            String formatedMessage = DateTime.getDateTime() + "[Private message from: " + nickname + "]: " + message;
            return formatedMessage;
        }

        private boolean validateNickname(String nickname, String id) {
            boolean isValid = true;

            for (ConnectionHandler conn : allConn) {
                if (conn != null) {
                    if (conn.getNickname().equals(nickname) && (!conn.getId().equals(id))) {
                        isValid = false;
                        break;
                    }
                }
            }

            return isValid;
        }

        public void closeConnection() {
            int index = 0;
            sendMessageToClient("[Servidor] Conexão finalizada.");
            for (ConnectionHandler conn : allConn) {
                if (conn.ID.equals(ID)) {
                    index = allConn.indexOf(conn);
                    allConn.remove(index);
                    break;
                }
            }

            try {
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                Log.saveLog(e.getMessage());
            }
        }
    }
}
