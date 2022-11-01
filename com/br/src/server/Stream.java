package com.br.src.server;

public class Stream {
    private String x;

    public Stream() {
    }

    public void println(String x) {
        synchronized(this) {
            notifyAll();
        }
        this.x = x;
    }

    public String readLine() {
        String data = this.x;
        this.x = null;
        return data;
    }
}
