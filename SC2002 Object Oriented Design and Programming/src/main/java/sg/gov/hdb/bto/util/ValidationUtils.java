package main.java.sg.gov.hdb.bto.util;

public class ValidationUtils {
    /**
     * Validate NRIC format (Singapore National Registration Identity Card)
     * @param nric The NRIC to validate
     * @return True if NRIC format is valid, false otherwise
     */
    public static boolean validateNRIC(String nric) {
        return nric != null && nric.matches("[ST]\\d{7}[A-Z]");
    }
    
    /**
     * Validate age is within a valid range
     * @param age The age to validate
     * @return True if age is valid, false otherwise
     */
    public static boolean validateAge(int age) {
        return age >= 18 && age <= 120; // Example range
    }
    
    /**
     * Validate flat type is one of the allowed types
     * @param flatType The flat type to validate
     * @return True if flat type is valid, false otherwise
     */
    public static boolean validateFlatType(String flatType) {
        return flatType != null && 
               (flatType.equals("2-Room") || 
                flatType.equals("3-Room") || 
                flatType.equals("4-Room") || 
                flatType.equals("5-Room"));
    }
}
