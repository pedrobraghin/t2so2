package test;
import com.br.src.security.RSA;

public class RSATest {
  public static void main(String[] args) {
    
    RSA rsa = new RSA();
    // rsa.generateKeyPair();
    String message = "Heelo, world!";
    String ecMessage = rsa.encrypt(message);
    String dcMessage = rsa.decrypt(ecMessage);
    System.out.println("0 - " + message);
    System.out.println("1 - " + ecMessage);
    System.out.println("2 - " + dcMessage);
  }
}