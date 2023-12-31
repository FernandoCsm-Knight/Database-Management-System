package crud.core.security;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;

import logic.SystemSpecification;


 /**
 * Class implementing RSA encryption and decryption.
 * It allows the creation of keys, loading of keys from file, encryption and decryption of messages.
 * Implements the {@link SystemSpecification} interface.
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 */
public class RSA implements SystemSpecification {

    private BigInteger n, d, e;
    private int bitlen = 1024;
    private static final String KEY_FILE_PATH = RSA_KEYS + "keys.txt";

    /**
     * Constructor for RSA class.
     * Initializes the bit length and tries to load keys from file.
     * If keys are not found, it generates new keys.
     *
     * @param bits The bit length for the RSA keys.
     */
    public RSA(int bits) {
        this.bitlen = bits;
        if (!loadKeysFromFile()) {
            generateKeys();
            saveKeysToFile();
        }
    }

    /**
     * Generates RSA keys using SecureRandom and BigInteger.
     */
    private void generateKeys() {
        SecureRandom r = new SecureRandom();
        BigInteger p = new BigInteger(bitlen / 2, 100, r);
        BigInteger q = new BigInteger(bitlen / 2, 100, r);
        n = p.multiply(q);
        BigInteger z = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        e = new BigInteger("3");

        while (z.gcd(e).intValue() > 1) {
            e = e.add(new BigInteger("2"));
        }

        d = e.modInverse(z);
    }

    /**
     * Loads RSA keys from a file.
     * 
     * @return true if keys were successfully loaded, false otherwise.
     */
    private boolean loadKeysFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(KEY_FILE_PATH))) {
            String modulus = br.readLine();
            String publicExp = br.readLine();
            String privateExp = br.readLine();

            if (modulus != null && publicExp != null && privateExp != null) {
                n = new BigInteger(modulus);
                e = new BigInteger(publicExp);
                d = new BigInteger(privateExp);
                return true;
            }
        } catch (IOException ex) {
            System.out.println("Erro ao ler as chaves do arquivo: " + ex.getMessage());
        }

        return false;
    }

    /**
     * Saves RSA keys to a file.
     */
    private void saveKeysToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(KEY_FILE_PATH))) {
            bw.write(n.toString());
            bw.newLine();
            bw.write(e.toString());
            bw.newLine();
            bw.write(d.toString());
        } catch (IOException ex) {
            System.out.println("Erro ao salvar as chaves no arquivo: " + ex.getMessage());
        }
    }

    /**
     * Encrypts a message using RSA encryption.
     * 
     * @param message The string to be encrypted.
     * @return The encrypted string, or null in case of encoding error.
     */
    public String encrypt(String message) {
        try {
            byte[] bytes = message.getBytes("UTF-8");
            byte[] bytesWithLeadingZero = new byte[bytes.length + 1];
            System.arraycopy(bytes, 0, bytesWithLeadingZero, 1, bytes.length);
            BigInteger messageBigInt = new BigInteger(bytesWithLeadingZero);
            return messageBigInt.modPow(e, n).toString();
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Erro de codificação: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Decrypts a message using RSA decryption.
     * 
     * @param message The encrypted string.
     * @return The decrypted string, or null in case of decoding error.
     */
    public String decrypt(String message) {
        BigInteger messageBigInt = new BigInteger(message);
        byte[] decryptedBytes = messageBigInt.modPow(d, n).toByteArray();

        if (decryptedBytes[0] == 0) {
            byte[] temp = new byte[decryptedBytes.length - 1];
            System.arraycopy(decryptedBytes, 1, temp, 0, temp.length);
            decryptedBytes = temp;
        }
    
        try {
            return new String(decryptedBytes, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Erro de decodificação: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Gets the public exponent of RSA.
     * 
     * @return The public exponent as a BigInteger.
     */
    public BigInteger getPublicExponent() {
        return e;
    }
    
    /**
     * Gets the modulus of RSA.
     * 
     * @return The modulus as a BigInteger.
     */
    public BigInteger getModulus() {
        return n;
    }

    /**
     * Gets the private exponent of RSA.
     * 
     * @return The private exponent as a BigInteger.
     */
    public BigInteger getPrivateExponent() {
        return d;
    }    
}
