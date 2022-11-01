package com.br.src.main;

import javax.swing.SwingUtilities;

import com.br.src.gui.Window;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Window();
            }
        });
    }
}
