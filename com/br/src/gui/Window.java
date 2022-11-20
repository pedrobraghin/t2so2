package com.br.src.gui;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.br.src.logger.Log;
import com.br.src.server.Client;
import com.br.src.server.Server;
import com.br.src.server.Stream;
import com.br.src.utils.DateTime;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Window extends JFrame {

    private final int WINDOW_WIDTH = 600;
    private final int WINDOW_HEIGHT = 500;
    private final int LINE_HEIGHT = 30;

    private JPanel mainPanel;

    private JButton sendMessageButton;
    private JButton clearChatButton;

    private JButton resetConnectionButton;
    private JButton initConnectionButton;

    private JButton resetHostButton;
    private JButton initHostButton;

    private JLabel connectionStatus;
    private JLabel hostStatus;
    private JLabel hostLabel;
    private JLabel portLabel;

    private JScrollPane scrollPane;
    private JTextArea messagesArea;
    private JTextField messageTextField;
    private JTextField hosTextField;
    private JTextField portTextField;

    private Thread messageThread;
    private Thread serverThread;
    private Server server;
    private Client client;

    private final int DEFAULT_PORT= 4567;
    private final String DEFAULT_HOST = "127.0.0.1";
    private int port;
    private String host;
    private ArrayList<String> messagesHistory;
    private int messagesHistoryIndex;
    private int messagesCount;

    public Window() {
        super("Aplicativo de mensagens");
        messagesHistory = new ArrayList<String>();
        messagesHistoryIndex = 0;
        messagesCount = 0;
        port = DEFAULT_PORT;
        host = DEFAULT_HOST;
        initWindow();
        initComponent();
        pack();
        setVisible(true);
        messageTextField.requestFocus();
    }

    private void initWindow() {
        setResizable(false);
        setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponent() {

        // main panel of the application
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        layout.setVgap(5);
        mainPanel = new JPanel(layout);
        mainPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        mainPanel.setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));


        // status panel
        FlowLayout statusPanelLayout = new FlowLayout(FlowLayout.LEFT);
        JPanel statusPanel = new JPanel(statusPanelLayout);
        statusPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        statusPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, 40));
        statusPanel.setSize(new Dimension(WINDOW_WIDTH, 40));

        connectionStatus = new JLabel("Status da conexão: desconectado");
        connectionStatus.setPreferredSize(new Dimension(WINDOW_WIDTH, 10));
        connectionStatus.setSize(new Dimension(WINDOW_WIDTH, LINE_HEIGHT));
        connectionStatus.setHorizontalAlignment(SwingConstants.LEFT);
        connectionStatus.setVerticalAlignment(0);
        connectionStatus.setForeground(Color.GRAY);

        hostStatus = new JLabel("Status do host local: inativo");
        hostStatus.setPreferredSize(new Dimension(WINDOW_WIDTH, 10));
        hostStatus.setSize(new Dimension(WINDOW_WIDTH, LINE_HEIGHT));
        hostStatus.setHorizontalAlignment(SwingConstants.LEFT);
        hostStatus.setVerticalAlignment(0);
        hostStatus.setForeground(Color.GRAY);

        statusPanel.add(connectionStatus);
        statusPanel.add(hostStatus);


        // control button panel
        FlowLayout lefLayout = new FlowLayout(FlowLayout.LEFT);
        JPanel buttonsPanel = new JPanel(lefLayout);
        buttonsPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        buttonsPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, 30));
        buttonsPanel.setSize(new Dimension(WINDOW_WIDTH, 30));

        Icon connectionIcon = new ImageIcon(getClass().getResource("/assets/connection.png"));
        initConnectionButton = new JButton(connectionIcon);
        initConnectionButton.setPreferredSize(new Dimension(20, 20));
        initConnectionButton.setSize(new Dimension(20, 20));
        initConnectionButton.addActionListener(initConnectionListener);
        initConnectionButton.setToolTipText("Iniciar/Parar conexão");

        Icon resetConnectionIcon = new ImageIcon(getClass().getResource("/assets/resetConnection.png"));
        resetConnectionButton = new JButton(resetConnectionIcon);
        resetConnectionButton.setPreferredSize(new Dimension(20, 20));
        resetConnectionButton.setSize(new Dimension(20, 20));
        resetConnectionButton.addActionListener(resetConnectionListener);
        resetConnectionButton.setToolTipText("Reiniciar conexão");

        Icon hostIcon = new ImageIcon(getClass().getResource("/assets/host.png"));
        initHostButton = new JButton(hostIcon);
        initHostButton.setPreferredSize(new Dimension(20, 20));
        initHostButton.setSize(new Dimension(20, 20));
        initHostButton.addActionListener(initHostListener);
        initHostButton.setToolTipText("Iniciar/Parar host");

        Icon resetHostIcon = new ImageIcon(getClass().getResource("/assets/resetHost.png"));
        resetHostButton = new JButton(resetHostIcon);
        resetHostButton.setPreferredSize(new Dimension(20, 20));
        resetHostButton.setSize(new Dimension(20, 20));
        resetHostButton.addActionListener(resetHostListener);
        resetHostButton.setToolTipText("Reiniciar host");

        Icon clearIcon = new ImageIcon(getClass().getResource("/assets/clear.png"));
        clearChatButton = new JButton(clearIcon);
        clearChatButton.setPreferredSize(new Dimension(20, 20));
        clearChatButton.setSize(new Dimension(20, 20));
        clearChatButton.addActionListener(clearChatListener);
        clearChatButton.setToolTipText("Limpar chat");

        buttonsPanel.add(initConnectionButton);
        buttonsPanel.add(resetConnectionButton);
        buttonsPanel.add(initHostButton);
        buttonsPanel.add(resetHostButton);
        buttonsPanel.add(clearChatButton);


        // Host and port panel
        FlowLayout hostAndPortLayout = new FlowLayout(FlowLayout.LEFT);
        JPanel portAndHostPanel = new JPanel(hostAndPortLayout);
        portAndHostPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        portAndHostPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, LINE_HEIGHT + 10));
        portAndHostPanel.setSize(new Dimension(WINDOW_WIDTH, LINE_HEIGHT + 10));

        hostLabel = new JLabel("Host:");
        hostLabel.setPreferredSize(new Dimension(30, 10));
        hostLabel.setSize(new Dimension(30, LINE_HEIGHT));
        hostLabel.setHorizontalAlignment(SwingConstants.LEFT);
        hostLabel.setVerticalAlignment(0);
        hostLabel.setForeground(Color.GRAY);

        hosTextField = new JTextField();
        hosTextField.setPreferredSize(new Dimension(200, LINE_HEIGHT));
        hosTextField.setSize(new Dimension(200, LINE_HEIGHT));

        portLabel = new JLabel("Port:");
        portLabel.setPreferredSize(new Dimension(30, 10));
        portLabel.setSize(new Dimension(30, LINE_HEIGHT));
        portLabel.setHorizontalAlignment(SwingConstants.LEFT);
        portLabel.setVerticalAlignment(0);
        portLabel.setForeground(Color.GRAY);

        portTextField = new JTextField();
        portTextField.setPreferredSize(new Dimension(100, LINE_HEIGHT));
        portTextField.setSize(new Dimension(100, LINE_HEIGHT));

        portAndHostPanel.add(hostLabel);
        portAndHostPanel.add(hosTextField);
        portAndHostPanel.add(portLabel);
        portAndHostPanel.add(portTextField);


        // Messages box area
        messagesArea = new JTextArea();
        messagesArea.setLineWrap(true);
        messagesArea.setWrapStyleWord(true);
        messagesArea.setEditable(false);

        scrollPane = new JScrollPane(messagesArea);
        scrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH - 20, 250));
        scrollPane.setSize(new Dimension(WINDOW_WIDTH - 20, 250));

        messageTextField = new JTextField();
        messageTextField.setPreferredSize(new Dimension(WINDOW_WIDTH - 150, LINE_HEIGHT));
        messageTextField.setSize(new Dimension(WINDOW_WIDTH - 150, LINE_HEIGHT));
        messageTextField.addKeyListener(textFieldKeyListener);

        sendMessageButton = new JButton("Enviar");
        sendMessageButton.setPreferredSize(new Dimension(100, LINE_HEIGHT));
        sendMessageButton.setSize(new Dimension(100, LINE_HEIGHT));
        sendMessageButton.addActionListener(sendMessageListener);
        sendMessageButton.setToolTipText("Enviar mensagem");

        mainPanel.add(portAndHostPanel);
        mainPanel.add(statusPanel);
        mainPanel.add(buttonsPanel);
        mainPanel.add(scrollPane);
        mainPanel.add(messageTextField);
        mainPanel.add(sendMessageButton);

        add(mainPanel);
        mainPanel.getRootPane().setDefaultButton(sendMessageButton);
    }

    private void toggleButtons(boolean hostBusttons) {
        if (hostBusttons) {
            this.initHostButton.setEnabled(!this.initHostButton.isEnabled());
            this.resetHostButton.setEnabled(!this.resetHostButton.isEnabled());
        } else {
            this.initConnectionButton.setEnabled(!this.initConnectionButton.isEnabled());
            this.resetConnectionButton.setEnabled(!this.resetConnectionButton.isEnabled());
        }
    }

    public void initConnection() {
        if (client != null && client.isConnected()) {
            closeConnection();
        } else {
            updateHostAndPort();
            this.messageThread = new Thread(new MessageHandler());
            this.messageThread.start();
        }
    }

    public void closeConnection() {
        try {
            client.closeConnection();
        } catch (Exception e) {
            Log.saveLog("Error on stop Client Connection Thread: " + e.getMessage());
        } finally {
            connectionStatus.setText("Status da conexão: desconectado");
        }
    }

    public void resetConnection() {
        try {
            if (client!= null && client.isConnected()) {
                client.sendMessage("/exit");
                closeConnection();
                initConnection();
            }
        } catch (Exception e) {
            Log.saveLog("Error on reset Client Connection: " + e.getMessage());
        }
    }

    public void initHost() {
        if (client != null && client.isConnected()) {
            closeConnection();
            messagesArea.append("Conexão com servidor externo finalizada.");
        }
        if (server != null && server.isServerRunning()) {
            closeHost();
        } else {
            try {
                toggleButtons(false);
                this.serverThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        server = new Server();
                        connectionStatus.setText("Status da conexão: desconectado");
                        hostStatus.setText("Status do host local: ativo");
                        messagesArea.append("\n" + DateTime.getDateTime() + " Iniciando servidor local...");
                        messagesArea.append("\n" + DateTime.getDateTime()
                                + " Clique no botão com o símbolo de wi-fi para se conectar ao servidor local.");
                        server.run();
                    }
                });
                this.serverThread.start();
            } catch(Exception e) {
                Log.saveLog("Error on Local Server Thread: " + e.getMessage());
            } finally {
                toggleButtons(false);
            }
        }
    }

    public void closeHost() {
        try {
            server.closeServer();
        } catch (Exception e) {
            Log.saveLog("Error on stop Server Connection Thread: " + e.getMessage());
        } finally {
            hostStatus.setText("Status do host local: host encerrado");
            messagesArea.append("\n" + DateTime.getDateTime() + "Servidor local finalizado.");
        }
    }

    public void resetHost() {
        try {
            if (server != null && server.isServerRunning()) {
                closeHost();
                initHost();
                connectionStatus.setText("Status do host local: host encerrado");
            }
        } catch (Exception e) {
            Log.saveLog("Error on reload Server Connection Thread: " + e.getMessage());
        }
    }

    private void updateHostAndPort() {
        String host = hosTextField.getText();
        String port = portTextField.getText();
        if (host.length() > 0 && !host.isBlank() && !host.isEmpty()) {
            this.host = host;
        } else {
            this.host = this.DEFAULT_HOST;
        }

        if (port.length() > 0 && !port.isBlank() && !port.isEmpty()) {
            try {
                int newPort =  Integer.parseInt(port);
                if(newPort > 0 && newPort < 65536) {
                    this.port = newPort;
                } else {
                    JOptionPane.showMessageDialog(this, "Insira uma porta entre 1 e 65535.", "Porta Inválida", JOptionPane.INFORMATION_MESSAGE);    
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Insira somente valores numéricos no campo \"Port\".", "Porta Inválida", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            this.port = this.DEFAULT_PORT;
        }
    }

    private void sendMessage() {
        String textFieldMessage = this.messageTextField.getText();
        if (textFieldMessage.length() > 0 && !textFieldMessage.isBlank() && !textFieldMessage.isEmpty()) {
            messagesCount++;
            messagesHistory.add(textFieldMessage);
            if (!client.isConnected()) {
                messagesArea.append("\n" + DateTime.getDateTime() + " Chat-Server indisponível!");
                return;
            }
            messagesHistoryIndex = 0;
            this.messageTextField.setText("");
            client.sendMessage(textFieldMessage);
            messagesArea.append("\n" + DateTime.getDateTime() + "[Você]: " + textFieldMessage);
        }
    }

    private void clearchat() {
        this.messagesArea.setText("");
    }

    private void getMessageInHistory(boolean down) {
        if (messagesCount == 0) {
            return;
        }
        String lastMessage = messagesHistory.get(messagesHistoryIndex);
        messageTextField.setText(lastMessage);
        if (down) {
            int num = messagesHistoryIndex - 1;
            if (num < 0) {
                num = messagesCount - 1;
            }
            messagesHistoryIndex = num;
        } else {
            messagesHistoryIndex = (messagesHistoryIndex + 1) % messagesCount;
        }
    }

    private ActionListener sendMessageListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            sendMessage();
        }
    };

    private ActionListener clearChatListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            clearchat();
        }
    };

    private ActionListener initConnectionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            initConnection();
        }
    };

    private ActionListener resetConnectionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            resetConnection();
        }
    };

    private ActionListener initHostListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            initHost();
        }
    };

    private ActionListener resetHostListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            resetHost();
        }
    };

    private KeyAdapter textFieldKeyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                getMessageInHistory(false);
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                getMessageInHistory(true);
            }
        }
    };

    private class MessageHandler implements Runnable {
        @Override
        public void run() {
            try {
                toggleButtons(true);
                connectionStatus.setText("Status da conexão: conectado ao host " + host + " : " + port);
                Stream stream = new Stream();
                client = new Client(host, port, stream);
                Thread clientThread = new Thread(client);
                clientThread.start();
                String message;

                do {
                    synchronized (stream) {
                        stream.wait();
                        if ((message = stream.readLine()) != null) {
                            messagesArea.append("\n" + message);
                        }
                    }
                } while (client.isConnected());
            } catch (Exception e) {
                Log.saveLog("Error on Client Message Handler: " + e.getMessage());
            } finally {
                toggleButtons(true);
                connectionStatus.setText("Status da conexão: desconectado");
            }
        }
    }
}
