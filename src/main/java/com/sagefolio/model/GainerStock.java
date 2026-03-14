
package com.sagefolio.model;

public class GainerStock {
    private String symbol;
    private double price;
    private double changePercent;
    private String volatilityLabel;    // "Low" / "Medium" / "High"
    private String compatibilityLabel; // from CompatibilityService
    private boolean weekend = false;

    public boolean isWeekend() { return weekend; }
    public void setWeekend(boolean weekend) { this.weekend = weekend; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getChangePercent() { return changePercent; }
    public void setChangePercent(double changePercent) { this.changePercent = changePercent; }

    public String getVolatilityLabel() { return volatilityLabel; }
    public void setVolatilityLabel(String volatilityLabel) { this.volatilityLabel = volatilityLabel; }

    public String getCompatibilityLabel() { return compatibilityLabel; }
    public void setCompatibilityLabel(String compatibilityLabel) { this.compatibilityLabel = compatibilityLabel; }
}
