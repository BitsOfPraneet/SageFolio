package com.sagefolio.model;

public class RiskProfile {
    private String name;
    private int score;
    private String category; // Conservative, Moderate, Aggressive
    private int horizon;
    private int income;
    private int experience;
    private int tolerance;

    public RiskProfile() {}

    public RiskProfile(String name, int score, String category) {
        this.name = name;
        this.score = score;
        this.category = category;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getHorizon() { return horizon; }
    public void setHorizon(int horizon) { this.horizon = horizon; }

    public int getIncome() { return income; }
    public void setIncome(int income) { this.income = income; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public int getTolerance() { return tolerance; }
    public void setTolerance(int tolerance) { this.tolerance = tolerance; }
}
