package org.opensingular.dbuserprovider.util;

import java.util.Base64;
import java.util.Objects;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF2SHA256HashingUtil {

    private char[] password;
    private byte[] salt;
    private int iterations;
    private static final int keyLength = 256;
    /**
     * @param password
     * @param salt
     * @param iterations
     */
    public PBKDF2SHA256HashingUtil(String password, String salt, int iterations){
        this.password = password.toCharArray();
        this.salt = salt.getBytes();
        this.iterations = iterations;
    }

    public boolean validatePassword(String passwordHash){
        return Objects.equals(passwordHash, hashPassword());
    }

    private String hashPassword(){
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(this.password, this.salt, this.iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            return "";
        }
    }
}
