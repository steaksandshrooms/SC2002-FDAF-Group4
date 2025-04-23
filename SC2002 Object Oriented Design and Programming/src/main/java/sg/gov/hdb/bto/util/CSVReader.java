package main.java.sg.gov.hdb.bto.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    private static final String RESOURCES_PATH = "resources/";
    
    /**
     * Read data from CSV file
     * @param filename The name of the CSV file to read
     * @return List of string arrays, each array representing a row of the CSV
     */
    public List<String[]> readCSV(String filename) {
        List<String[]> data = new ArrayList<>();
        File resourcesDir = new File(RESOURCES_PATH);
        
        // Check if resources directory exists
        if (!resourcesDir.exists()) {
            System.out.println("Resources directory does not exist: " + resourcesDir.getAbsolutePath());
            return data;
        }
        
        File file = new File(RESOURCES_PATH + filename);
        
        // Check if file exists
        if (!file.exists()) {
            System.out.println("File does not exist: " + file.getAbsolutePath());
            return data;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                data.add(values);
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }
        
        return data;
    }
    
    /**
     * Read data from CSV file, skipping the header row
     * @param filename The name of the CSV file to read
     * @return List of string arrays, each array representing a data row (no headers)
     */
    public List<String[]> readCSVSkipHeader(String filename) {
        List<String[]> allRows = readCSV(filename);
        List<String[]> dataRows = new ArrayList<>();
        
        // Skip the header row (first row)
        for (int i = 1; i < allRows.size(); i++) {
            dataRows.add(allRows.get(i));
        }
        
        return dataRows;
    }
}