package test;

import com.br.src.security.RSA;

public class Main {
    public static void main(String[] args) {
        RSA rsa = new RSA();
        rsa.generateKeyPair();
    }
}
