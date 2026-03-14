package com.sagefolio.service;

import org.springframework.stereotype.Service;

@Service
public class RiskScoringService {

    public int calculateScore(int horizon, int income, int experience, int tolerance) {
        int h = (int) ((horizon     / 3.0) * 25);
        int i = (int) ((income      / 3.0) * 20);
        int e = (int) ((experience  / 3.0) * 20);
        int t = (int) (((tolerance - 1) / 9.0) * 35);
        return Math.min(100, h + i + e + t);
    }

    public String getCategory(int score) {
        if (score < 40) return "Conservative";
        if (score < 70) return "Moderate";
        return "Aggressive";
    }
}
