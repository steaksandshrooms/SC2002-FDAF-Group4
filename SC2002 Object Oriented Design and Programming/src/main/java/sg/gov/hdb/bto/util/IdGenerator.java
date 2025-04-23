package main.java.sg.gov.hdb.bto.util;

import java.util.UUID;

public class IdGenerator {
    /**
     * Generate a unique ID with a prefix
     * @param prefix Prefix to add to the ID (e.g., "APP" for applications)
     * @return A unique ID string
     */
    public static String generateId(String prefix) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return prefix + "-" + uuid;
    }
}
