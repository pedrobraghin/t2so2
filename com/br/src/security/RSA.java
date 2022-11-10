package com.br.src.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;

import com.br.src.logger.Log;

public class RSA {

    private static final String PATH_PRIVATE_KEY = "./keys/private.key";
    private static final String PATH_PUBLIC_KEY = "./keys/public.key";
    private static final String ALGORITHM = "RSA";

    public String encrypt(String message) {
        byte[] cipherText = null;
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(PATH_PUBLIC_KEY));
            PublicKey publicKey = (PublicKey) inputStream.readObject();
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            cipherText = cipher.doFinal(message.getBytes());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.saveLog("Error while encrypting: " + e.getMessage());
        }

        return encode(cipherText);
    }

    private String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public String decrypt(String encryptedMessage) {
        byte[] encryptedBytes = decode(encryptedMessage);
        String plainText = null;
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(PATH_PUBLIC_KEY));
            PrivateKey privateKey = (PrivateKey) inputStream.readObject();

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedText = cipher.doFinal(encryptedBytes);
            inputStream.close();
            plainText = new String(decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
            Log.saveLog("Error while decrypting: " + e.getMessage());
        }
        return plainText;
    }

    private byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public void generateKeyPair() {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(1024);
            final KeyPair key = keyGen.generateKeyPair();

            File privateKeyFile = new File(PATH_PRIVATE_KEY);
            File publicKeyFile = new File(PATH_PUBLIC_KEY);

            privateKeyFile.createNewFile();
            publicKeyFile.createNewFile();

            ObjectOutputStream chavePublicaOS = new ObjectOutputStream(
                    new FileOutputStream(publicKeyFile));
            chavePublicaOS.writeObject(key.getPublic());
            chavePublicaOS.close();

            // Salva a Chave Privada no arquivo
            ObjectOutputStream chavePrivadaOS = new ObjectOutputStream(
                    new FileOutputStream(privateKeyFile));
            chavePrivadaOS.writeObject(key.getPrivate());
            chavePrivadaOS.close();
            System.out.println(key.getPublic());
            System.out.println();
            System.out.println();
            System.out.println(key.getPrivate().getClass());
        } catch (Exception e) {
            Log.saveLog("Error on generate key pair: " + e.getMessage());
        }

    }

}
