package test;

import com.br.src.security.RSA;

public class Main {
    public static void main(String[] args) {
        String message = "Olá mundo!";
        RSA rsa = new RSA();
        String messageEncrypted = rsa.encrypt(message);
        System.out.println(rsa.decrypt(messageEncrypted));
        rsa.generateKeyPair();
    }
}
