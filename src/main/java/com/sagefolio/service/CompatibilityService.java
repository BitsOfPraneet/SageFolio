package com.sagefolio.service;

import org.springframework.stereotype.Service;

@Service
public class CompatibilityService {

    public String getLabel(String investor, String volatility) {
        if (investor.equals("Conservative")) {
            if (volatility.equals("Low"))    return "Good Match";
            if (volatility.equals("Medium")) return "Moderate Match";
            return "High Risk Mismatch";
        }
        if (investor.equals("Moderate")) {
            if (volatility.equals("Low"))    return "Good Match";
            if (volatility.equals("Medium")) return "Good Match";
            return "Moderate Match";
        }
        // Aggressive
        if (volatility.equals("Low")) return "Moderate Match";
        return "Good Match";
    }

    public int getScore(String investor, String volatility) {
        return switch (getLabel(investor, volatility)) {
            case "Good Match"         -> 82;
            case "Moderate Match"     -> 55;
            case "High Risk Mismatch" -> 24;
            default -> 50;
        };
    }
}
