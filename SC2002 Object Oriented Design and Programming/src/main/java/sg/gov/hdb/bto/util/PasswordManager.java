package main.java.sg.gov.hdb.bto.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class PasswordManager {
    /**
     * Hash a password using SHA-256
     * @param password The plain text password
     * @return The hashed password as a hexadecimal string
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error hashing password: " + e.getMessage());
            return password; // Fallback to plaintext if hashing fails
        }
    }
    
    /**
     * Validate if a password meets security requirements
     * @param password The password to validate
     * @return True if password is valid, false otherwise
     */
    public static boolean validatePassword(String password) {
        // Example validation: password should be at least 8 characters long
        return password != null && password.length() >= 8;
    }
}