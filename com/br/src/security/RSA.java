package com.br.src.security;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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
            File publicKeyFile = new File(PATH_PUBLIC_KEY);
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());

            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            cipherText = cipher.doFinal(message.getBytes());
            return encode(cipherText);
        } catch (Exception e) {
            e.printStackTrace();
            Log.saveLog("Error while encrypting: " + e.getMessage());
        }

        return "Erro ao encriptar menssagem!";
    }

    private String encode(byte[] data) throws Exception{
        return Base64.getUrlEncoder().encodeToString(data);
    }

    public String decrypt(String encryptedMessage) {
        try {
            byte[] encryptedBytes = decode(encryptedMessage);
            File privateKeyFile = new File(PATH_PRIVATE_KEY);
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());

            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decryptedText = cipher.doFinal(encryptedBytes);
            String plainText = new String(decryptedText);
            return plainText;
        } catch (Exception e) {
            e.printStackTrace();
            Log.saveLog("Error while decrypting: " + e.getMessage());
        }
        return "Erro ao desencriptar menssagem!";
    }

    private byte[] decode(String data) throws Exception{
        return Base64.getUrlDecoder().decode(data);
    }

    public void generateKeyPair() {
        try {
            
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(2048);
            KeyPair keys = keyGen.generateKeyPair();
            File privateKeyFile = new File(PATH_PRIVATE_KEY);
            File publicKeyFile = new File(PATH_PUBLIC_KEY);

            privateKeyFile.createNewFile();
            publicKeyFile.createNewFile();

            FileOutputStream fos = new FileOutputStream(publicKeyFile);
            fos.write(keys.getPublic().getEncoded());
            fos.close();

            // Salva a Chave Privada no arquivo
            fos = new FileOutputStream(privateKeyFile);
            fos.write(keys.getPrivate().getEncoded());
            fos.close();
        } catch (Exception e) {
            Log.saveLog("Error on generate key pair: " + e.getMessage());
        }

    }

}
