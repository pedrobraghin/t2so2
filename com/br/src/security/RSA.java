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
    private static final String TOKEN_SEPARATOR = "--TOKEN--";

    public String encrypt(String message) {
        try {
            File publicKeyFile = new File(PATH_PUBLIC_KEY);
            KeyFactory keyFactory;
            EncodedKeySpec publicKeySpec;
            PublicKey publicKey;
            Cipher cipher;

            byte[] cipherText = null;
            String messageTokenyzed = "";
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
            String[] tokens = message.split(" ");
            keyFactory = KeyFactory.getInstance(ALGORITHM);
            publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = keyFactory.generatePublic(publicKeySpec);
            cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            
            // don't allow to add a last token separator at the end of the message
            int count = 0;

            for (String token : tokens) {
                cipherText = cipher.doFinal(token.getBytes());
                if(count < (tokens.length - 1)) {
                    messageTokenyzed += encode(cipherText) + TOKEN_SEPARATOR;
                } else {
                    messageTokenyzed += encode(cipherText);
                }
                count++;
            }

            return messageTokenyzed;
        } catch (Exception e) {
            e.printStackTrace();
            Log.saveLog("Error while encrypting: " + e.getMessage());
        }

        return "Erro ao encriptar menssagem!";
    }

    public String decrypt(String encryptedMessage) {
        try {
            File privateKeyFile = new File(PATH_PRIVATE_KEY);
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
            byte[] encryptedBytes;
            byte[] decryptedBytes;
            Cipher cipher;
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            String[] tokens = encryptedMessage.split(TOKEN_SEPARATOR);
            String plainText = "";

            keyFactory = KeyFactory.getInstance(ALGORITHM);
            privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
            
            int count = 0;
            for(String token : tokens) {  
                cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                encryptedBytes = decode(token);
                decryptedBytes = cipher.doFinal(encryptedBytes);
                if(count < (tokens.length -1)) {
                    plainText += new String(decryptedBytes) + " ";
                } else {
                    plainText += new String(decryptedBytes);
                }
            }
            return plainText;
        } catch (Exception e) {
            e.printStackTrace();
            Log.saveLog("Error while decrypting: " + e.getMessage());
        }
        return "Erro ao desencriptar menssagem!";
    }

    private String encode(byte[] data) throws Exception {
        return Base64.getUrlEncoder().encodeToString(data);
    }

    private byte[] decode(String data) throws Exception {
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
