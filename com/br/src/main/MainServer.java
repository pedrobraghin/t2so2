package com.br.src.main;

import com.br.src.server.Server;

public class MainServer {
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}