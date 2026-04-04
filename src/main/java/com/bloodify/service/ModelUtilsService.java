package com.bloodify.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ModelUtilsService {
    
    private final List<String> CLASSES = Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    private final Random random = new Random();
    
    public Map<String, Object> predictBloodGroup(String base64Image) {
        String predicted = CLASSES.get(random.nextInt(CLASSES.size()));
        
        // Mock confidence between 85 and 99
        int confidence = 85 + random.nextInt(15);
        
        // Generate mock probabilities summing to 100
        Map<String, Integer> probabilities = new HashMap<>();
        int remaining = 100 - confidence;
        for(String bg : CLASSES) {
            if(bg.equals(predicted)) {
                probabilities.put(bg, confidence);
            } else {
                int p = (CLASSES.indexOf(bg) == CLASSES.size() - 1) ? remaining : random.nextInt(remaining / 2 + 1);
                probabilities.put(bg, p);
                remaining -= p;
            }
        }
        
        String level = confidence >= 95 ? "High" : (confidence >= 90 ? "Medium" : "Low");
        
        Map<String, Object> result = new HashMap<>();
        result.put("bloodGroup", predicted);
        result.put("confidenceScore", confidence);
        result.put("confidenceLevel", level);
        result.put("distribution", probabilities);
        return result;
    }
    
    public List<String> getCompatibleDonors(String receiverBg) {
        switch(receiverBg) {
            case "A+": return Arrays.asList("A+", "A-", "O+", "O-");
            case "A-": return Arrays.asList("A-", "O-");
            case "B+": return Arrays.asList("B+", "B-", "O+", "O-");
            case "B-": return Arrays.asList("B-", "O-");
            case "AB+": return Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
            case "AB-": return Arrays.asList("AB-", "A-", "B-", "O-");
            case "O+": return Arrays.asList("O+", "O-");
            case "O-": return Arrays.asList("O-");
            default: return Arrays.asList();
        }
    }
}
